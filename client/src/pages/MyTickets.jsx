// src/pages/MyTickets.jsx
import { useEffect, useMemo, useState, useContext } from 'react';
import {
    Box, Heading, Text, Badge, VStack, HStack, Spinner,
    Center, Button, Card, CardBody, CardHeader, CardFooter
} from '@chakra-ui/react';
import { Link } from 'react-router-dom';
import AuthContext from '../context/AuthContext';
import TicketService from '../services/TicketService';

function fmt(dt) {
    if (!dt) return '';
    try {
        return new Intl.DateTimeFormat(undefined, {
            year: 'numeric', month: 'short', day: '2-digit',
            hour: '2-digit', minute: '2-digit'
        }).format(new Date(dt));
    } catch { return String(dt); }
}

export default function MyTickets() {
    const { user } = useContext(AuthContext);
    const [loading, setLoading] = useState(true);
    const [tickets, setTickets] = useState([]);
    const [error, setError] = useState('');
    const ticketService = useMemo(() => new TicketService(), []);

    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                const { data } = await ticketService.getMine(localStorage.getItem('token'));
                if (mounted) setTickets(data || []);
            } catch (e) {
                if (mounted) setError(e?.message || 'Failed to load tickets');
            } finally {
                if (mounted) setLoading(false);
            }
        })();
        return () => { mounted = false; };
    }, [ticketService]);

    if (loading) return <Center h="50vh"><Spinner /></Center>;
    if (error) return <Center h="50vh"><Heading size="md" color="red.500">{error}</Heading></Center>;

    return (
        <Center mt={8}>
            <VStack w="xl" spacing={4} align="stretch">
                <Heading size="lg">My Tickets</Heading>

                {tickets.length === 0 && (
                    <Box p={6} borderWidth="1px" borderRadius="lg">
                        <Text>You don’t have tickets yet.</Text>
                        <Button as={Link} to="/home" mt={3} colorScheme="pink">Browse Events</Button>
                    </Box>
                )}

                {tickets.map(t => (
                    <Card key={t.id}>
                        <CardHeader>
                            <HStack justify="space-between">
                                <Heading size="sm">Ticket #{t.id}</Heading>
                                {t.status && (
                                    <Badge colorScheme={
                                        t.status === 'ACTIVE' ? 'green' :
                                            t.status === 'USED' ? 'blue' :
                                                t.status === 'CANCELLED' ? 'red' : 'gray'
                                    }>
                                        {t.status}
                                    </Badge>
                                )}
                            </HStack>
                        </CardHeader>
                        <CardBody>
                            <VStack align="start" spacing={1}>
                                <Text><b>Code:</b> {t.code}</Text>
                                <Text><b>Created:</b> {fmt(t.createdAt)}</Text>
                                <Text><b>Event:</b> <Link to={`/post/${t.postId}`} style={{color:'#d53f8c'}}>View event</Link></Text>
                                {/* Якщо додаси в TicketResponse поле postTitle — покажемо: */}
                                {t.postTitle && <Text><b>Title:</b> {t.postTitle}</Text>}
                                {t.postStartAt && <Text><b>Starts:</b> {fmt(t.postStartAt)}</Text>}
                            </VStack>
                        </CardBody>
                        <CardFooter>
                            <HStack>
                                <Button as={Link} to={`/post/${t.postId}`} variant="outline">Open Event</Button>
                            </HStack>
                        </CardFooter>
                    </Card>
                ))}
            </VStack>
        </Center>
    );
}
