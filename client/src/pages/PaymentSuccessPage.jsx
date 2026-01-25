
import { useEffect, useMemo, useState } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { Center, Spinner, Text } from '@chakra-ui/react'
import TicketService from '../services/TicketService'

export default function PaymentSuccessPage() {
    const [params] = useSearchParams()
    const postId = params.get('postId')
    const nav = useNavigate()
    const ticketService = useMemo(() => new TicketService(), [])
    const [msg, setMsg] = useState('Підтверджуємо оплату та створюємо квиток...')

    useEffect(() => {
        if (!postId) {
            setMsg('Missing postId in URL')
            return
        }

        let alive = true
        let tries = 0
        const maxTries = 20

        const tick = async () => {
            tries++
            try {
                await ticketService.getMy(postId)
                if (!alive) return
                nav(`/posts/${postId}`, { replace: true })
            } catch {
                if (!alive) return
                if (tries >= maxTries) {
                    setMsg('Квиток ще не зʼявився. Онови сторінку події через кілька секунд.')
                } else {
                    setTimeout(tick, 1000)
                }
            }
        }

        tick()
        return () => { alive = false }
    }, [postId, nav, ticketService])

    return (
        <Center h="60vh" flexDir="column" gap={3}>
            <Spinner />
            <Text>{msg}</Text>
        </Center>
    )
}
