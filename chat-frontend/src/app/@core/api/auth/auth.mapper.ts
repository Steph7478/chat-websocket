import { AuthMeDto, AuthUser } from './auth.types';

export function mapAuthUser(dto: AuthMeDto): AuthUser {
    return {
        username: dto.username
    };
}
