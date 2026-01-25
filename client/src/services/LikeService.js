import { api } from '../api/api';

export default class LikeService {
    add(userId, postId) {
        return api.post('likes/add', { userId, postId });
    }

    delete(userId, postId) {
        return api.post('likes/delete', { userId, postId });
    }

    isLiked(userId, postId) {
        return api.get('likes/isliked', { params: { userId, postId } });
    }

    getLikesByPost(postId) {
        return api.get(`likes/getallbypost/${postId}`);
    }
}
