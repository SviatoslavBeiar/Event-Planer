import { api } from '../api/api';

export default class CommentService {
    getAllByPost(postId) {
        return api.get(`comments/getallbypost/${postId}`);
    }

    add(values) {
        return api.post('comments/add', values);
    }
}
