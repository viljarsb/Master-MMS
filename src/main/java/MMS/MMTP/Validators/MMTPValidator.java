package MMS.MMTP.Validators;

import MMS.MMTP.*;
import MMS.Misc.MrnValidator;
import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class MMTPValidator
{
    public static boolean validate(ApplicationMessageTest message) {
        String messageID = message.getId();
        try {
            UUID uuid = UUID.fromString(messageID);
        } catch (IllegalArgumentException e) {
            return false;
        }

        ApplicationMessageTest.DestinationCase destinationCase = message.getDestinationCase();

        if (destinationCase == ApplicationMessageTest.DestinationCase.RECIPIENTS) {
            RepeatedRecipients recipients = message.getRecipients();
            for (String recipient : recipients.getRecipientsList()) {
                if (!MrnValidator.validate(recipient)) {
                    return false;
                }
            }
        } else if (destinationCase == ApplicationMessageTest.DestinationCase.SUBJECT) {
            String subject = message.getSubject();
            if (subject.length() > 100 || subject.length() == 0) {
                return false;
            }
        } else {
            return false;
        }

        String sender = message.getSender();
        if (!MrnValidator.validate(sender)) {
            return false;
        }

        if (message.hasExpires()) {
            Timestamp expires = message.getExpires();
            Instant expiresInstant = Instant.ofEpochSecond(expires.getSeconds(), expires.getNanos());
            Instant currentInstant = Instant.now();
            if (expiresInstant.isBefore(currentInstant) || expiresInstant.isAfter(currentInstant.plus(30, ChronoUnit.DAYS))) {
                return false;
            }
        }

        if (message.getPayload().size() == 0) {
            return false;
        }

        return true;
    }


    public static boolean validate(Register message)
    {
        List<String> recipients = message.getInterestsList();

        for (String recipient : recipients) {
            if (!MrnValidator.validate(recipient)) {
                return false;
            }
        }

        if(message.getInterestsCount() == 0 && !message.hasWantDirectMessages())
        {
            return false;
        }

        return false;
    }


    public static boolean validate(Unregister message)
    {
        List<String> recipients = message.getInterestsList();

        for (String recipient : recipients) {
            if (!MrnValidator.validate(recipient)) {
                return false;
            }
        }

        if(message.getInterestsCount() == 0 && !message.hasWantDirectMessages())
        {
            return false;
        }

        return false;
    }
}
