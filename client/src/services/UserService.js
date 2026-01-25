import { api } from '../api/api';

export default class UserService {
    getById(id) {
        return api.get(`users/getbyid/${id}`);
    }

    isFollowing(userId, followingId) {
        return api.get('users/isfollowing', { params: { userId, followingId } });
    }

    getByEmail(email) {
        return api.get('users/getbyemail', { params: { email } });
    }
}
