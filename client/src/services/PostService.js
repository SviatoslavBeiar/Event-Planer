import axios from 'axios';

export default class PostService{
    add(values, token){
        return axios.post(process.env.REACT_APP_API + "posts/add", values, {
            headers: { 'Authorization': "Bearer " + token }
        })
    }

    getAllByUserId(userId, token){
        return axios.get(process.env.REACT_APP_API + "posts/getallbyuser/" + userId, {
            headers: { 'Authorization': "Bearer " + token }
        })
    }

    getAllByUserFollowing(userId, token){
        return axios.get(process.env.REACT_APP_API + "posts/getbyuserfollowing/" + userId, {
            headers: { 'Authorization': "Bearer " + token }
        })
    }

    getById(id, token){
        return axios.get(process.env.REACT_APP_API + "posts/getbyid/" + id, {
            headers: { 'Authorization': "Bearer " + token }
        })
    }

    // ➕ ДОДАНО: глобальний фід
    getAll(token){
        // ⚠️ Якщо у тебе інший шлях (наприклад "posts/all" або "posts/feed"),
        // змінити рядок нижче відповідно.
        return axios.get(process.env.REACT_APP_API + "posts/getall", {
            headers: { 'Authorization': "Bearer " + token }
        })
    }


    // ✅ FIXED: use REACT_APP_API (not undefined `API`)
    updateStatus(postId, status, token) {
        return axios.put(
            process.env.REACT_APP_API + `posts/${postId}/status`,
            { status }, // "PUBLISHED" | "DRAFT" | "CANCELLED"
            { headers: { Authorization: 'Bearer ' + token } }
        );
    }
}
