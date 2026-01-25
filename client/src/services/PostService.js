import { api } from '../api/api';

export default class PostService {
    add(values) {
        return api.post('posts/add', values);
    }

    getAllByUserId(userId) {
        return api.get(`posts/getallbyuser/${userId}`);
    }

    getAllByUserFollowing(userId) {
        return api.get(`posts/getbyuserfollowing/${userId}`);
    }

    getById(id) {
        return api.get(`posts/getbyid/${id}`);
    }

    getAll() {
        return api.get('posts/getall');
    }

    updateStatus(postId, status) {
        return api.put(`posts/${postId}/status`, { status });
    }
}
