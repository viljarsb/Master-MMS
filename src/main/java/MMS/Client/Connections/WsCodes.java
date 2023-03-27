package MMS.Client.Connections;

public enum WsCodes
{
    NORMAL_CLOSURE(1000),
    GOING_AWAY(1001),
    PROTOCOL_ERROR(1002),
    UNSUPPORTED_DATA(1003),
    NO_STATUS_RECEIVED(1005),
    ABNORMAL_CLOSURE(1006),
    INVALID_FRAME_PAYLOAD_DATA(1007),
    POLICY_VIOLATION(1008),
    MESSAGE_TOO_BIG(1009),
    MISSING_EXTENSION(1010),
    INTERNAL_ERROR(1011),
    SERVICE_RESTART(1012),
    TRY_AGAIN_LATER(1013),
    BAD_GATEWAY(1014),
    TLS_HANDSHAKE_FAILURE(1015);

    private final int code;

    WsCodes(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}