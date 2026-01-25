import { api } from '../api/api';

export default class PostImageService {
    upload(values) {
        return api.post('postimages/upload', values, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    }


    download(id) {
        return api.get(`postimages/download/${id}`, { responseType: 'blob' });
    }
}
