import React, { useEffect, useState, useMemo } from 'react';
import { Box, Heading, HStack, Stat, StatLabel, StatNumber, Stack, Table, Thead, Tbody, Tr, Th, Td, Spinner } from '@chakra-ui/react';
import AnalyticsService from '../services/AnalyticsService';

export default function AnalyticsPanel({ postId, days = 7 }) {
    const token = localStorage.getItem('token');
    const analyticsService = useMemo(() => new AnalyticsService(), []);
    const [loading, setLoading] = useState(true);
    const [data, setData] = useState(null);

    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                setLoading(true);
                const res = await analyticsService.getPostAnalytics(Number(postId), token, days);
                if (mounted) setData(res.data);
            } catch (e) {
                console.error(e);
            } finally {
                if (mounted) setLoading(false);
            }
        })();
        return () => { mounted = false; }
    }, [postId, days, analyticsService, token]);

    if (loading) return <Spinner />;

    if (!data) return null;

    return (
        <Box borderWidth="1px" borderRadius="md" p={4} bg="white">
            <Heading size="sm" mb={3}>Tickets analytics (last {days} days)</Heading>
            <HStack spacing={8} mb={4}>
                <Stat>
                    <StatLabel>Tickets sold</StatLabel>
                    <StatNumber>{data.ticketsSoldTotal}</StatNumber>
                </Stat>
                <Stat>
                    <StatLabel>Capacity</StatLabel>
                    <StatNumber>{data.capacity ?? '—'}</StatNumber>
                </Stat>
                <Stat>
                    <StatLabel>Remaining</StatLabel>
                    <StatNumber>{data.remaining ?? '—'}</StatNumber>
                </Stat>
                {data.revenue != null && (
                    <Stat>
                        <StatLabel>Revenue</StatLabel>
                        <StatNumber>{new Intl.NumberFormat(undefined,{style:'currency',currency: 'USD'}).format(data.revenue)}</StatNumber>
                    </Stat>
                )}
            </HStack>

            <Box>
                <Heading size="xs" mb={2}>Sales by day</Heading>
                <Table size="sm">
                    <Thead><Tr><Th>Date</Th><Th isNumeric>Count</Th></Tr></Thead>
                    <Tbody>
                        {data.salesByDay && data.salesByDay.map(d => (
                            <Tr key={d.day}><Td>{d.day}</Td><Td isNumeric>{d.count}</Td></Tr>
                        ))}
                    </Tbody>
                </Table>
            </Box>
        </Box>
    );
}
