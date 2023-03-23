package MMS.Client;

public class DisconnectionReason
{
    private final String reason;
    private final StatusCode statusCode;


    public DisconnectionReason(String reason, int statusCode)
    {
        this.reason = reason;

        switch (statusCode)
        {
            case 1000 -> this.statusCode = StatusCode.NORMAL_CLOSURE;
            case 1001 -> this.statusCode = StatusCode.GOING_AWAY;
            case 1002 -> this.statusCode = StatusCode.PROTOCOL_ERROR;
            case 1003 -> this.statusCode = StatusCode.UNSUPPORTED_DATA;
            case 1005 -> this.statusCode = StatusCode.NO_STATUS_RECEIVED;
            case 1006 -> this.statusCode = StatusCode.ABNORMAL_CLOSURE;
            case 1007 -> this.statusCode = StatusCode.INVALID_FRAME_PAYLOAD_DATA;
            case 1008 -> this.statusCode = StatusCode.POLICY_VIOLATION;
            case 1009 -> this.statusCode = StatusCode.MESSAGE_TOO_BIG;
            case 1010 -> this.statusCode = StatusCode.MISSING_EXTENSION;
            case 1011 -> this.statusCode = StatusCode.INTERNAL_ERROR;
            case 1012 -> this.statusCode = StatusCode.SERVICE_RESTART;
            case 1013 -> this.statusCode = StatusCode.TRY_AGAIN_LATER;
            case 1014 -> this.statusCode = StatusCode.BAD_GATEWAY;
            case 1015 -> this.statusCode = StatusCode.TLS_HANDSHAKE_FAILURE;
            default -> this.statusCode = null;
        }
    }


    public StatusCode getStatus()
    {
        return statusCode;
    }


    public String getReason()
    {
        return reason;
    }


    public enum StatusCode
    {
        NORMAL_CLOSURE,
        GOING_AWAY,
        PROTOCOL_ERROR,
        UNSUPPORTED_DATA,
        NO_STATUS_RECEIVED,
        ABNORMAL_CLOSURE,
        INVALID_FRAME_PAYLOAD_DATA,
        POLICY_VIOLATION,
        MESSAGE_TOO_BIG,
        MISSING_EXTENSION,
        INTERNAL_ERROR,
        SERVICE_RESTART,
        TRY_AGAIN_LATER,
        BAD_GATEWAY,
        TLS_HANDSHAKE_FAILURE
    }
}
