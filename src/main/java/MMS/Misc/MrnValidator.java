package MMS.Misc;

import java.util.regex.Pattern;

public class MrnValidator
{
    public static boolean validate(String mrn)
    {
        String pattern = "^urn:mrn:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,19}[a-zA-Z0-9]:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,31}[a-zA-Z0-9]:[\\p{Alnum}%._~/-]+(/[\\p{Alnum}%._~/-]+)*(#\\p{Alnum}+)?$";
        return Pattern.matches(pattern, mrn);
    }
}
