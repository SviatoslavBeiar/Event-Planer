import { api } from '../api/api';

export default class EventCheckerService {
    list(postId) {
        return api.get(`event-checkers/by-post/${postId}`);
    }

    byPost(postId) {
        return this.list(postId);
    }

    assign(postId, userId) {
        return api.post(`event-checkers/assign/${postId}/${userId}`, {});
    }

    remove(postId, userId) {
        return api.delete(`event-checkers/revoke/${postId}/${userId}`);
    }

    revoke(postId, userId) {
        return this.remove(postId, userId);
    }

    amIChecker(postId) {
        return api.get(`event-checkers/am-i-checker/${postId}`);
    }

    mine() {
        return api.get('event-checkers/mine');
    }

    assignByEmail(postId, email) {
        return api.post(
            `event-checkers/assign-by-email/${postId}`,
            {},
            { params: { email } }
        );
    }

    removeByEmail(postId, email) {
        return api.delete(`event-checkers/revoke-by-email/${postId}`, {
            params: { email },
        });
    }
}
