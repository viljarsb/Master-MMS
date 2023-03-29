package MMS.Misc;

public class SubjectValidator
{
    public static boolean validate(String s)
    {
        return s.length() <= 100 && s.length() >= 1;
    }
}
