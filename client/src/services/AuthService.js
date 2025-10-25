import { api } from '../api/api';

export default class AuthService {
    register(values) {
        return api.post('auth/register', values);
    }
    login(values) {
        return api.post('auth/login', values);
    }
}
