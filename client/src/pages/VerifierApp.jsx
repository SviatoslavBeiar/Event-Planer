import { useParams } from 'react-router-dom';
import { useMemo, useState } from 'react';
import {
    Box, Button, Heading, Input, Stack, Text,
    Alert, AlertIcon, AlertTitle, AlertDescription,
    HStack, Badge, useToast
} from '@chakra-ui/react';
import TicketService from '../services/TicketService';

const MESSAGES = {
    OK: 'The ticket is valid and ready for entry.',
    CONSUMED: 'The ticket has been successfully consumed.',
    FORBIDDEN: 'You are not authorized to verify this event.',
    TICKET_NOT_FOUND: 'Ticket not found.',
    TICKET_FOR_ANOTHER_EVENT: 'This ticket belongs to another event.',
    TICKET_NOT_ACTIVE: 'Ticket is not active.',
};

function human(msg) {
    return MESSAGES[msg] || msg || 'An unexpected error occurred.';
}

export default function VerifierApp() {
    const { postId } = useParams();
    const svc = useMemo(() => new TicketService(), []);
    const toast = useToast();

    const [code, setCode] = useState('');
    const [res, setRes] = useState(null);   // TicketVerifyResponse or null
    const [loading, setLoading] = useState(false);
    const [consuming, setConsuming] = useState(false);
    const [err, setErr] = useState('');

    const token = localStorage.getItem('token');

    const onVerify = async () => {
        setErr('');
        setRes(null);
        const trimmed = (code || '').trim();
        if (!trimmed) {
            setErr('Please enter a ticket code.');
            return;
        }
        try {
            setLoading(true);
            const r = await svc.verifyValidate(Number(postId), trimmed, token);
            setRes(r.data);
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Verification failed.';
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
                title: ok ? 'Ticket consumed' : 'Not consumed',
                description: human(r.data?.message),
                status: ok ? 'success' : 'error',
                duration: 3000,
                isClosable: true
            });
        } catch (e) {
            const msg = e?.response?.data?.message || e?.response?.data || e?.message || 'Consumption failed.';
            toast({
                title: 'Error',
                description: msg,
                status: 'error',
                duration: 4000,
                isClosable: true
            });
        } finally {
            setConsuming(false);
        }
    };

    const onKey = (e) => {
        if (e.key === 'Enter') onVerify();
    };

    return (
        <Box p={6} maxW="700px">
            <Heading size="md" mb={4}>
                Ticket verification for event #{postId}
            </Heading>

            <Stack direction="row" spacing={3} mb={3}>
                <Input
                    value={code}
                    onChange={(e) => setCode(e.target.value.toUpperCase())}
                    onKeyDown={onKey}
                    placeholder="Enter ticket code"
                    autoFocus
                />
                <Button colorScheme="pink" onClick={onVerify} isLoading={loading}>
                    Verify
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
                                {res.ownerFullName && <Text>Owner: {res.ownerFullName}</Text>}
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
                        <Button
                            variant="ghost"
                            onClick={() => {
                                setRes(null);
                                setCode('');
                            }}
                        >
                            Clear
                        </Button>
                    </HStack>
                </>
            )}
        </Box>
    );
}
