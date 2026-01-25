import { api } from '../api/api';

export default class FollowService {
    follow(values) {
        return api.post('follows/add', values);
    }

    unfollow(values) {
        return api.post('follows/delete', values);
    }
}
