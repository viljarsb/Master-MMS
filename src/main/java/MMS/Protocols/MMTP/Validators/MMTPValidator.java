package MMS.Protocols.MMTP.Validators;


import MMS.Client.Exceptions.MMTPValidationException;
import MMS.Misc.MrnValidator;
import MMS.Protocols.MMTP.MessageFormats.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


/**
 * A utility class for validating MMTP messages.
 */
public class MMTPValidator
{

    public static void validate(DirectApplicationMessage message) throws MMTPValidationException
    {
        // Validate id
        try
        {
            UUID.fromString(message.getId());
        }

        catch (IllegalArgumentException e)
        {
            throw new MMTPValidationException("Message id is not a valid UUID");
        }

        // Validate recipients
        for (String recipient : message.getRecipientsList())
        {
            if (!MrnValidator.validate(recipient))
            {
                throw new MMTPValidationException("Destination: " + recipient + " is not a valid MRN");
            }
        }

        // Validate sender
        if (!MrnValidator.validate(message.getSender()))
        {
            throw new MMTPValidationException("Sender: " + message.getSender() + " is not a valid MRN");
        }

        // Validate timestamp
        if (message.hasExpires())
        {
            Instant now = Instant.now();
            Instant expireTime = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
            if (expireTime.isBefore(now) || expireTime.isAfter(now.plus(30, ChronoUnit.DAYS)))
            {
                throw new MMTPValidationException("Message expires at: " + expireTime + " which is outside the allowed range");
            }
        }

        // Validate payload
        if(message.getPayload().size() < 1)
        {
            throw new MMTPValidationException("Message payload is empty");
        }
    }


    public static void validate(SubjectCastApplicationMessage message) throws MMTPValidationException
    {
        // Validate id
        try
        {
            UUID.fromString(message.getId());
        }

        catch (IllegalArgumentException e)
        {
            throw new MMTPValidationException("Message id is not a valid UUID");
        }

        // Validate subject
        if (message.getSubject().length() > 100 || message.getSubject().length() < 1)
        {
            throw new MMTPValidationException("Subject: " + message.getSubject() + " is not a valid subject");
        }

        // Validate sender
        if (!MrnValidator.validate(message.getSender()))
        {
            throw new MMTPValidationException("Sender: " + message.getSender() + " is not a valid MRN");
        }

        // Validate timestamp
        if (message.hasExpires())
        {
            Instant now = Instant.now();
            Instant expireTime = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
            if (expireTime.isBefore(now) || expireTime.isAfter(now.plus(30, ChronoUnit.DAYS)))
            {
                throw new MMTPValidationException("Message expires at: " + expireTime + " which is outside the allowed range");
            }
        }

        // Validate payload
        if(message.getPayload().size() < 1)
        {
            throw new MMTPValidationException("Message payload is empty");
        }
    }



    public static void validate(Register message) throws MMTPValidationException
    {
        // Validate interests
        List<String> interests = message.getInterestsList();

        if (interests.isEmpty() && !message.hasWantDirectMessages())
        {
            throw new MMTPValidationException("Register message is empty");
        }

        for (String interest : interests)
        {
            if (interest == null || interest.length() < 1 || interest.length() > 100)
            {
                throw new MMTPValidationException("Subject: " + interest + " is not a valid subject");
            }
        }

        // Validate want_direct_messages
        if (message.hasWantDirectMessages() && !message.getWantDirectMessages())
        {
            throw new MMTPValidationException("want_direct_messages must be true when present");
        }
    }



    public static void validate(Unregister message) throws MMTPValidationException
    {
        // Validate interests
        List<String> interests = message.getInterestsList();

        if (interests.isEmpty() && !message.hasWantDirectMessages())
        {
            throw new MMTPValidationException("Register message is empty");
        }

        for (String interest : interests)
        {
            if (interest == null || interest.length() < 1 || interest.length() > 100)
            {
                throw new MMTPValidationException("Subject: " + interest + " is not a valid subject");
            }
        }

        // Validate want_direct_messages
        if (message.hasWantDirectMessages() && !message.getWantDirectMessages())
        {
            throw new MMTPValidationException("want_direct_messages must be false when present");
        }
    }


    /**
     * Validates a RoutingUpdate message.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    public static boolean validate(RoutingUpdate message)
    {
        // Validate MRNs and subjects
        boolean hasMrn = false;
        boolean hasSubject = false;

        for (String mrn : message.getMRNsList())
        {
            if (!MrnValidator.validate(mrn))
            {
                return false;
            }
            hasMrn = true;
        }

        for (String subject : message.getSubjectsList())
        {
            if (subject == null || subject.length() < 1 || subject.length() > 100)
            {
                return false;
            }
            hasSubject = true;
        }

        // Check if there is at least one MRN or one subject
        if (!hasMrn && !hasSubject)
        {
            return false;
        }

        // All checks passed
        return true;
    }
}