package MMS.MMTP.Validators;


import MMS.MMTP.MessageFormats.*;
import MMS.Misc.MrnValidator;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;


/**
 * A utility class for validating MMTP messages.
 */
public class MMTPValidator
{

    /**
     * Validates a DirectApplicationMessage.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    public static boolean validate(DirectApplicationMessage message)
    {
        // Validate id
        try
        {
            UUID.fromString(message.getId());
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }

        // Validate recipients
        for (String recipient : message.getRecipientsList())
        {
            if (!MrnValidator.validate(recipient))
            {
                return false;
            }
        }

        // Validate sender
        if (!MrnValidator.validate(message.getSender()))
        {
            return false;
        }

        // Validate timestamp
        if (message.hasExpires())
        {
            Instant now = Instant.now();
            Instant expireTime = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
            if (expireTime.isBefore(now) || expireTime.isAfter(now.plus(30, ChronoUnit.DAYS)))
            {
                return false;
            }
        }

        // Validate payload
        return message.getPayload().size() > 0;
    }


    /**
     * Validates a SubjectCastApplicationMessage.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    public static boolean validate(SubjectCastApplicationMessage message)
    {
        // Validate id
        try
        {
            UUID.fromString(message.getId());
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }

        // Validate subject
        String subject = message.getSubject();
        if (subject.length() < 1 || subject.length() > 100)
        {
            return false;
        }

        // Validate sender
        if (!MrnValidator.validate(message.getSender()))
        {
            return false;
        }

        // Validate timestamp
        if (message.hasExpires())
        {
            Instant now = Instant.now();
            Instant expireTime = Instant.ofEpochSecond(message.getExpires().getSeconds(), message.getExpires().getNanos());
            if (expireTime.isBefore(now) || expireTime.isAfter(now.plus(30, ChronoUnit.DAYS)))
            {
                return false;
            }
        }

        // Validate payload
        return message.getPayload().size() > 0;
    }


    /**
     * Validates a Register message.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    public static boolean validate(Register message)
    {
        // Validate interests
        List<String> interests = message.getInterestsList();
        if (interests.isEmpty())
        {
            return false;
        }

        for (String interest : interests)
        {
            if (interest == null || interest.length() < 1 || interest.length() > 100)
            {
                return false;
            }
        }

        // Validate want_direct_messages
        if (message.hasWantDirectMessages() && !message.getWantDirectMessages())
        {
            return false;
        }

        // All checks passed
        return true;
    }


    /**
     * Validates an Unregister message.
     *
     * @param message the message to validate
     * @return true if the message is valid, false otherwise
     */
    public static boolean validate(Unregister message)
    {
        // Validate interests
        List<String> interests = message.getInterestsList();
        if (interests.isEmpty())
        {
            return false;
        }

        for (String interest : interests)
        {
            if (interest == null || interest.length() < 1 || interest.length() > 100)
            {
                return false;
            }
        }

        // Validate want_direct_messages
        if (message.hasWantDirectMessages() && message.getWantDirectMessages())
        {
            return false;
        }

        // All checks passed
        return true;
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