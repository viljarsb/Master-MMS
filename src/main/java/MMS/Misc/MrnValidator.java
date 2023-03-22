package MMS.Misc;

import java.util.regex.Pattern;

public class MrnValidator
{
    public static boolean validate(String mrn)
    {
        //Pattern from https://github.com/maritimeconnectivity/MRNService/blob/master/src/main/java/net/maritimeconnectivity/mrnservice/MRNValidationService.java
        String pattern = "^[Uu][Rr][Nn]\\:[Mm][Rr][Nn]\\:([A-Za-z0-9]([A-Za-z0-9]|\\-){0,20}[A-Za-z0-9])\\:([A-Za-z0-9][-A-Za-z0-9]{0,20}[A-Za-z0-9])\\:((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)|\\/)*)((\\?\\+((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)|\\/|\\?)*))?(\\?\\=((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)|\\/|\\?)*))?)?(#(((([-A-Z._a-z0-9]|~)|%[0-9A-Fa-f][0-9A-Fa-f]|(\\!|\\$|&|'|\\(|\\)|\\*|\\+|,|;|\\=)|\\:|@)|\\/|\\?)*))?$";
        return Pattern.matches(pattern, mrn);
    }
}
