import { useEffect, useMemo, useState, useContext, useRef } from 'react';
import {
    Box, Heading, Text, Badge, VStack, HStack, Spinner,
    Center, Button, Card, CardBody, CardHeader, CardFooter,
    Modal, ModalOverlay, ModalContent, ModalHeader, ModalCloseButton, ModalBody,
    useToast
} from '@chakra-ui/react';
import { Link } from 'react-router-dom';
import QRCode from 'react-qr-code';
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
    const [qrOpen, setQrOpen] = useState(false);
    const [qrTicket, setQrTicket] = useState(null); // { code, postId, id, ... }
    const bigQrWrapRef = useRef(null);
    const toast = useToast();

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

    const openQr = (t) => { setQrTicket(t); setQrOpen(true); };
    const closeQr = () => { setQrOpen(false); setQrTicket(null); };

    const downloadSvg = () => {
        try {
            const svg = bigQrWrapRef.current?.querySelector('svg');
            if (!svg) return;
            const source = new XMLSerializer().serializeToString(svg);
            const blob = new Blob([source], { type: 'image/svg+xml;charset=utf-8' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `ticket-${qrTicket?.code || 'qr'}.svg`;
            a.click();
            URL.revokeObjectURL(url);
            toast({ title: 'QR saved', status: 'success', duration: 2000, isClosable: true });
        } catch {
            toast({ title: 'Save failed', status: 'error', duration: 2500, isClosable: true });
        }
    };

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
                            <HStack align="start" spacing={4}>
                                <Box p={2} bg="white" borderRadius="md" borderWidth="1px">
                                    <QRCode value={t.code} size={64} />
                                </Box>

                                <VStack align="start" spacing={1} flex="1">
                                    <Text><b>Code:</b> {t.code}</Text>
                                    <Text><b>Created:</b> {fmt(t.createdAt)}</Text>
                                    <Text><b>Event:</b> <Link to={`/post/${t.postId}`} style={{ color: '#d53f8c' }}>View event</Link></Text>
                                    {t.postTitle && <Text><b>Title:</b> {t.postTitle}</Text>}
                                    {t.postStartAt && <Text><b>Starts:</b> {fmt(t.postStartAt)}</Text>}
                                </VStack>
                            </HStack>
                        </CardBody>

                        <CardFooter>
                            <HStack>
                                <Button as={Link} to={`/post/${t.postId}`} variant="outline">Open Event</Button>
                                <Button onClick={() => openQr(t)} colorScheme="pink" variant="solid">
                                    Open QR
                                </Button>
                            </HStack>
                        </CardFooter>
                    </Card>
                ))}
            </VStack>

            {/* Модалка з великою версією QR */}
            <Modal isOpen={qrOpen} onClose={closeQr} isCentered size="md">
                <ModalOverlay />
                <ModalContent>
                    <ModalHeader>Ticket QR</ModalHeader>
                    <ModalCloseButton />
                    <ModalBody pb={6}>
                        <VStack spacing={3} align="center">
                            <Box ref={bigQrWrapRef} p={3} bg="white" borderRadius="md" borderWidth="1px">
                                {qrTicket && <QRCode value={qrTicket.code} size={256} />}
                            </Box>
                            {qrTicket && (
                                <>
                                    <Text fontSize="sm"><b>Code:</b> {qrTicket.code}</Text>
                                    <HStack>
                                        <Button onClick={downloadSvg} variant="outline">Download SVG</Button>
                                        <Button onClick={closeQr} colorScheme="pink">Close</Button>
                                    </HStack>
                                </>
                            )}
                        </VStack>
                    </ModalBody>
                </ModalContent>
            </Modal>
        </Center>
    );
}
