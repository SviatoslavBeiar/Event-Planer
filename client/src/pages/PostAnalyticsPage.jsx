
import React, { useContext, useEffect, useState } from 'react';
import { useParams, Navigate, Link as RouterLink } from 'react-router-dom';
import { Center, Spinner, Heading, Box, Button } from '@chakra-ui/react';
import AuthContext from '../context/AuthContext';
import PostService from '../services/PostService';
import AnalyticsPanel from '../components/AnalyticsPanel';

export default function PostAnalyticsPage() {
    const { postId } = useParams();
    const { user } = useContext(AuthContext);
    const [loading, setLoading] = useState(true);
    const [post, setPost] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                const service = new PostService();
                const token = localStorage.getItem('token');
                const res = await service.getById(Number(postId), token);
                if (!mounted) return;
                setPost(res.data);
            } catch (e) {
                setError(e?.response?.data || e?.message || 'Failed to load post');
            } finally {
                if (mounted) setLoading(false);
            }
        })();
        return () => { mounted = false; };
    }, [postId]);

    if (loading) return <Center h="60vh"><Spinner size="lg" /></Center>;
    if (error) return <Center h="60vh"><Heading size="md" color="red.500">{String(error)}</Heading></Center>;

    const isOrganizer =
        user &&
        post &&
        Number(user.id) === Number(post.userId) &&
        user.role === 'ORGANIZER';

    if (!isOrganizer) {
        return <Navigate to="/home" replace />;
    }

    return (
        <Box maxW="lg" mx="auto" mt={8} p={4}>
            <Heading size="lg" mb={4}>
                {post.title} — Analytics
            </Heading>

            <AnalyticsPanel
                postId={post.id}
                days={7}
                showAttendance={true}
            />

            <Box mt={4} display="flex" justifyContent="flex-end">
                <Button
                    as={RouterLink}
                    to={`/post/${post.id}`}
                    variant="ghost"
                >
                    Back to event
                </Button>
            </Box>
        </Box>
    );
}
