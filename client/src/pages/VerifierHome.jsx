import { useEffect, useMemo, useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import { Box, Button, Heading, Stack, Text } from '@chakra-ui/react';
import EventCheckerService from '../services/EventCheckerService';
import AuthContext from '../context/AuthContext';

export default function VerifierHome() {
    const { user } = useContext(AuthContext);
    const svc = useMemo(() => new EventCheckerService(), []);
    const [items, setItems] = useState([]);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (!token) return;
        svc.mine(token).then(res => setItems(res.data || [])).catch(() => setItems([]));
    }, [svc]);

    return (
        <Box p={6}>
            <Heading size="md" mb={4}>My events (checker)</Heading>
            <Stack>
                {items.length === 0 && <Text color="gray.500">No assigned events yet.</Text>}
                {items.map(ec => (
                    <Button key={ec.id} as={Link} to={`/verify/${ec.postId}`} variant="outline">
                        {ec.postTitle ? ec.postTitle : `Event #${ec.postId}`}
                    </Button>
                ))}
            </Stack>
        </Box>
    );
}
