export type ChatMessageType =
    | 'TEXT'
    | 'ENCRYPTED_MSG'
    | 'KEY_EXCHANGE'
    | 'KEY_EXCHANGE_ACK'
    | 'GET_PUB_KEY'
    | 'PUB_KEY_RESPONSE'
    | 'USER_LIST';

export interface ChatMessageDto {
    from: string;
    to: string;
    payload?: string;
    publicKey?: string;
    type: ChatMessageType;
}

export interface ChatMessage {
    to?: string;
    from: string;
    text: string;
    private: boolean;
    time: string;
}