
import { useEffect, useMemo, useState } from 'react';
import {
    Box, Heading, HStack, VStack, Input, Button, useToast,
    Tag, TagLabel, TagCloseButton, Text, Divider
} from '@chakra-ui/react';
import EventCheckerService from '../services/EventCheckerService';

export default function EventCheckersPanel({ postId }) {
    const svc = useMemo(() => new EventCheckerService(), []);
    const [checkers, setCheckers] = useState([]);
    const [emailInput, setEmailInput] = useState('');
    const [loading, setLoading] = useState(false);
    const toast = useToast();
    const token = localStorage.getItem('token');

    const load = async () => {
        try {
            const { data } = await svc.list(postId, token);
            setCheckers(data);
        } catch (e) {
            toast({ title: 'Failed to load checkers', status: 'error', duration: 3000 });
        }
    };

    useEffect(() => { load(); /* eslint-disable-next-line */ }, [postId]);

    const onAdd = async () => {
        const email = (emailInput || '').trim();
        if (!email) { toast({ title: 'Enter email', status: 'warning' }); return; }
        try {
            setLoading(true);
            await svc.assignByEmail(postId, email, token);
            setEmailInput('');
            await load();
            toast({ title: 'Checker assigned', status: 'success', duration: 2500 });
        } catch (e) {
            const msg = e?.response?.data?.message || e?.message || 'Assign failed';
            toast({ title: 'Assign failed', description: msg, status: 'error', duration: 5000 });
        } finally {
            setLoading(false);
        }
    };

    const onRemove = async (email) => {
        try {
            setLoading(true);
            await svc.removeByEmail(postId, email, token);
            await load();
            toast({ title: 'Checker removed', status: 'success', duration: 2500 });
        } catch (e) {
            const msg = e?.response?.data?.message || e?.message || 'Remove failed';
            toast({ title: 'Remove failed', description: msg, status: 'error', duration: 5000 });
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box borderWidth="1px" rounded="xl" p={4}>
            <Heading size="sm" mb={3}>Event checkers</Heading>

            <HStack mb={3}>
                <Input
                    placeholder="User email"
                    value={emailInput}
                    onChange={(e) => setEmailInput(e.target.value)}
                    maxW="280px"
                />
                <Button onClick={onAdd} isLoading={loading} colorScheme="pink">Add</Button>
            </HStack>

            <Divider my={2} />

            {checkers.length === 0 ? (
                <Text color="gray.500" fontSize="sm">No checkers yet</Text>
            ) : (
                <VStack align="start" spacing={2}>
                    {checkers.map(ch => (
                        <Tag key={ch.id} size="lg" colorScheme="purple" borderRadius="full">
                            <TagLabel>{ch.userFullName} — {ch.userEmail}</TagLabel> {/* ✅ показуємо емейл */}
                            <TagCloseButton onClick={() => onRemove(ch.userEmail)} />
                        </Tag>
                    ))}
                </VStack>
            )}
        </Box>
    );
}
