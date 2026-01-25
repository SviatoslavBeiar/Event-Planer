import React, { useEffect, useState, useMemo } from 'react';
import {
    Box, Heading, HStack, Stat, StatLabel, StatNumber,
    Table, Thead, Tbody, Tr, Th, Td, Spinner
} from '@chakra-ui/react';
import AnalyticsService from '../services/AnalyticsService';

export default function AnalyticsPanel({ postId, days = 7, showAttendance = false }) {
    const token = localStorage.getItem('token');
    const analyticsService = useMemo(() => new AnalyticsService(), []);
    const [loading, setLoading] = useState(true);
    const [data, setData] = useState(null);

    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                setLoading(true);
                const res = await analyticsService.getPostAnalytics(
                    Number(postId),
                    token,
                    days
                );
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

    const attendanceRate =
        data.ticketsSoldTotal > 0
            ? Math.round((data.ticketsUsedTotal * 100) / data.ticketsSoldTotal)
            : 0;

    return (
        <Box borderWidth="1px" borderRadius="md" p={4} bg="white">
            <Heading size="sm" mb={3}>
                Tickets analytics (last {days} days)
            </Heading>

            {/* --- SUMMARY --- */}
            <HStack spacing={8} mb={4} wrap="wrap">
                <Stat>
                    <StatLabel>Tickets sold</StatLabel>
                    <StatNumber>{data.ticketsSoldTotal}</StatNumber>
                </Stat>

                {showAttendance && (
                    <Stat>
                        <StatLabel>Tickets used</StatLabel>
                        <StatNumber color="green.500">
                            {data.ticketsUsedTotal}
                        </StatNumber>
                    </Stat>
                )}

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
                        <StatNumber>
                            {new Intl.NumberFormat(undefined, {
                                style: 'currency',
                                currency: 'USD'
                            }).format(data.revenue)}
                        </StatNumber>
                    </Stat>
                )}

                {showAttendance && (
                    <Stat>
                        <StatLabel>Attendance rate</StatLabel>
                        <StatNumber>{attendanceRate}%</StatNumber>
                    </Stat>
                )}
            </HStack>

            {/* --- SALES BY DAY --- */}
            <Box mb={4}>
                <Heading size="xs" mb={2}>Sales by day</Heading>
                <Table size="sm">
                    <Thead>
                        <Tr>
                            <Th>Date</Th>
                            <Th isNumeric>Count</Th>
                        </Tr>
                    </Thead>
                    <Tbody>
                        {data.salesByDay?.map(d => (
                            <Tr key={d.day}>
                                <Td>{d.day}</Td>
                                <Td isNumeric>{d.count}</Td>
                            </Tr>
                        ))}
                    </Tbody>
                </Table>
            </Box>

            {/* --- ATTENDANCE BY DAY --- */}
            {showAttendance && (
                <Box>
                    <Heading size="xs" mb={2}>Attendance by day</Heading>
                    <Table size="sm">
                        <Thead>
                            <Tr>
                                <Th>Date</Th>
                                <Th isNumeric>Used tickets</Th>
                            </Tr>
                        </Thead>
                        <Tbody>
                            {data.attendanceByDay?.map(d => (
                                <Tr key={d.day}>
                                    <Td>{d.day}</Td>
                                    <Td isNumeric>{d.count}</Td>
                                </Tr>
                            ))}
                        </Tbody>
                    </Table>
                </Box>
            )}
        </Box>
    );
}
