import { ChatMessage, ChatMessageDto } from './chat.types';

export function mapChatMessage(dto: ChatMessageDto): ChatMessage {
    return {
        from: dto.from,
        text: dto.payload ?? '',
        private: dto.type === 'ENCRYPTED_MSG'
    };
}
