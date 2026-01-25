import { useParams } from 'react-router-dom';
import { useMemo, useState } from 'react';
import {
    Box, Button, Heading, Input, Stack, Text,
    Alert, AlertIcon, AlertTitle, AlertDescription,
    HStack, Badge, useToast
} from '@chakra-ui/react';
import TicketService from '../services/TicketService';
import QrScannerModal from '../components/QrScannerModal';

const MESSAGES = {
    OK: 'Ticket is valid and ready to enter',
    CONSUMED: 'Ticket has been consumed',
    FORBIDDEN: 'You are not allowed to verify this event',
    TICKET_NOT_FOUND: 'Ticket not found',
    TICKET_FOR_ANOTHER_EVENT: 'Ticket belongs to another event',
    TICKET_NOT_ACTIVE: 'Ticket is not active',
};

const human = (msg) => MESSAGES[msg] || msg || 'An error occurred';

export default function VerifierApp() {
    const { postId } = useParams();
    const svc = useMemo(() => new TicketService(), []);
    const toast = useToast();

    const [code, setCode] = useState('');
    const [res, setRes] = useState(null);
    const [loading, setLoading] = useState(false);
    const [consuming, setConsuming] = useState(false);
    const [err, setErr] = useState('');
    const [scanOpen, setScanOpen] = useState(false);

    const token = localStorage.getItem('token');

    const parseQrText = (text) => {
        // Підтримуємо "TICKET:<postId>:<code>" і просто "<code>"
        try {
            const s = String(text || '').trim().toUpperCase();
            if (s.startsWith('TICKET:')) {
                const parts = s.split(':'); // ["TICKET", "<POSTID>", "<CODE>"]
                if (parts.length >= 3) {
                    return { code: parts.slice(2).join(':'), qrPostId: Number(parts[1]) };
                }
            }
            return { code: s, qrPostId: null };
        } catch {
            return { code: '', qrPostId: null };
        }
    };

    const onVerify = async () => {
        setErr('');
        setRes(null);
        const trimmed = (code || '').trim();
        if (!trimmed) { setErr('Enter ticket code'); return; }
        try {
            setLoading(true);
            const r = await svc.verifyValidate(Number(postId), trimmed, token);
            setRes(r.data);
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Verification error';
            setErr(msg);
        } finally {
            setLoading(false);
        }
    };

    const onConsume = async () => {
        if (!res?.valid) return;
        try {
            setConsuming(true);
            const r = await svc.verifyConsume(Number(postId), code.trim(), token);
            setRes(r.data);
            const ok = r.data?.valid && r.data?.message === 'CONSUMED';
            toast({
                title: ok ? 'Consumed' : 'Not consumed',
                description: human(r.data?.message),
                status: ok ? 'success' : 'error',
                duration: 3000, isClosable: true
            });
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Consume error';
            toast({ title: 'Error', description: msg, status: 'error', duration: 4000, isClosable: true });
        } finally {
            setConsuming(false);
        }
    };

    const onKey = (e) => { if (e.key === 'Enter') onVerify(); };

    const handleDetected = async (qrText) => {
        // закриваємо модалку, розбираємо пейлоад
        setScanOpen(false);
        const { code: parsedCode, qrPostId } = parseQrText(qrText);

        if (!parsedCode) {
            toast({ title: 'Invalid QR', status: 'error' });
            return;
        }

        // Якщо у QR є postId і він інший — попереджаємо
        if (qrPostId != null && qrPostId !== Number(postId)) {
            toast({
                title: 'QR for another event',
                description: `QR says event #${qrPostId}, you are on #${postId}`,
                status: 'warning', duration: 4000, isClosable: true
            });
            // все одно підставимо код — можливо, це перенос
        }

        setCode(parsedCode);
        // Автоматично запускаємо валідацію
        try {
            setLoading(true);
            const r = await svc.verifyValidate(Number(postId), parsedCode, token);
            setRes(r.data);
            if (!r.data?.valid) {
                toast({ title: 'Invalid ticket', description: human(r.data?.message), status: 'error' });
            }
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Verification error';
            setErr(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box p={6} maxW="700px">
            <Heading size="md" mb={4}>Verify tickets for event #{postId}</Heading>

            <Stack direction={{ base: 'column', sm: 'row' }} spacing={3} mb={3}>
                <Input
                    value={code}
                    onChange={(e) => setCode(e.target.value.toUpperCase())}
                    onKeyDown={onKey}
                    placeholder="Enter or scan ticket code"
                    autoFocus
                />
                <Button colorScheme="pink" onClick={onVerify} isLoading={loading}>
                    Verify
                </Button>
                <Button variant="outline" onClick={() => setScanOpen(true)}>
                    Scan QR
                </Button>
            </Stack>

            {err && (
                <Alert status="error" mb={3}>
                    <AlertIcon />
                    <AlertTitle mr={2}>Error</AlertTitle>
                    <AlertDescription>{human(err)}</AlertDescription>
                </Alert>
            )}

            {res && (
                <>
                    {res.valid ? (
                        <Alert status="success" mb={3}>
                            <AlertIcon />
                            <Stack spacing={1}>
                                <HStack>
                                    <Text fontWeight="bold">Ticket:</Text>
                                    <Badge>{res.code}</Badge>
                                    <Badge colorScheme="green">VALID</Badge>
                                </HStack>
                                {res.ownerFullName && <Text>User: {res.ownerFullName}</Text>}
                                {res.postTitle && <Text>Event: {res.postTitle}</Text>}
                                <Text>{human(res.message)}</Text>
                            </Stack>
                        </Alert>
                    ) : (
                        <Alert status="error" mb={3}>
                            <AlertIcon />
                            <Stack spacing={1}>
                                <HStack>
                                    <Text fontWeight="bold">Ticket:</Text>
                                    {res.code && <Badge>{res.code}</Badge>}
                                    <Badge colorScheme="red">INVALID</Badge>
                                </HStack>
                                {!!res.status && <Text>Status: {res.status}</Text>}
                                <Text>{human(res.message)}</Text>
                            </Stack>
                        </Alert>
                    )}

                    <HStack>
                        <Button
                            colorScheme="purple"
                            onClick={onConsume}
                            isDisabled={!res.valid}
                            isLoading={consuming}
                        >
                            Consume (USED)
                        </Button>
                        <Button variant="ghost" onClick={() => { setRes(null); setCode(''); }}>
                            Clear
                        </Button>
                    </HStack>
                </>
            )}

            <QrScannerModal
                isOpen={scanOpen}
                onClose={() => setScanOpen(false)}
                onDetected={handleDetected}
            />
        </Box>
    );
}
