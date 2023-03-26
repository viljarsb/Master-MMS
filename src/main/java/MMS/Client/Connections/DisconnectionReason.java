package MMS.Client.Connections;

/**
 * DisconnectionReason represents the reason and status code for a WebSocket
 * disconnection event. It provides a convenient mapping of integer status codes
 * to a more descriptive enumeration.
 */
public class DisconnectionReason
{
    private final String reason;
    private final WsCodes wsCodes;

    /**
     * Constructs a new DisconnectionReason with the specified reason and status code.
     *
     * @param reason     the reason for the disconnection
     * @param statusCode the integer status code for the disconnection
     */
    public DisconnectionReason(String reason, int statusCode)
    {
        this.reason = reason;

        switch (statusCode)
        {
            case 1000 -> this.wsCodes = WsCodes.NORMAL_CLOSURE;
            case 1001 -> this.wsCodes = WsCodes.GOING_AWAY;
            case 1002 -> this.wsCodes = WsCodes.PROTOCOL_ERROR;
            case 1003 -> this.wsCodes = WsCodes.UNSUPPORTED_DATA;
            case 1005 -> this.wsCodes = WsCodes.NO_STATUS_RECEIVED;
            case 1006 -> this.wsCodes = WsCodes.ABNORMAL_CLOSURE;
            case 1007 -> this.wsCodes = WsCodes.INVALID_FRAME_PAYLOAD_DATA;
            case 1008 -> this.wsCodes = WsCodes.POLICY_VIOLATION;
            case 1009 -> this.wsCodes = WsCodes.MESSAGE_TOO_BIG;
            case 1010 -> this.wsCodes = WsCodes.MISSING_EXTENSION;
            case 1011 -> this.wsCodes = WsCodes.INTERNAL_ERROR;
            case 1012 -> this.wsCodes = WsCodes.SERVICE_RESTART;
            case 1013 -> this.wsCodes = WsCodes.TRY_AGAIN_LATER;
            case 1014 -> this.wsCodes = WsCodes.BAD_GATEWAY;
            case 1015 -> this.wsCodes = WsCodes.TLS_HANDSHAKE_FAILURE;
            default -> this.wsCodes = null;
        }
    }

    /**
     * Returns the status code for the disconnection.
     *
     * @return the status code
     */
    public WsCodes getStatus()
    {
        return wsCodes;
    }


    /**
     * Returns the reason for the disconnection.
     *
     * @return the reason
     */
    public String getReason()
    {
        return reason;
    }

}
