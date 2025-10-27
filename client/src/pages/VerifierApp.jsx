// src/pages/VerifierApp.jsx
import { useParams } from 'react-router-dom';
import { useMemo, useState } from 'react';
import { Box, Button, Heading, Input, Stack, Text, Alert, AlertIcon } from '@chakra-ui/react';
import TicketService from '../services/TicketService';

export default function VerifierApp() {
    const { postId } = useParams();
    const svc = useMemo(() => new TicketService(), []);
    const [code, setCode] = useState('');
    const [result, setResult] = useState(null);
    const [err, setErr] = useState('');

    const verify = async () => {
        setErr(''); setResult(null);
        try {
            const token = localStorage.getItem('token');
            const res = await svc.verify(Number(postId), code.trim(), token);
            setResult(res.data);
        } catch (e) {
            setErr(e?.response?.data?.message || e?.response?.data || e?.message || 'Verification failed');
        }
    };

    return (
        <Box p={6}>
            <Heading size="md" mb={4}>Verify tickets for event #{postId}</Heading>
            <Stack direction="row" spacing={3} mb={3}>
                <Input value={code} onChange={(e)=>setCode(e.target.value)} placeholder="Enter ticket code"/>
                <Button colorScheme="pink" onClick={verify}>Verify</Button>
            </Stack>
            {err && <Alert status="error"><AlertIcon/>{err}</Alert>}
            {result && (
                <Alert status="success">
                    <AlertIcon/>
                    Ticket OK. User: {result.userFullName} — marked USED.
                </Alert>
            )}
        </Box>
    );
}
