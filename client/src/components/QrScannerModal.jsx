// src/components/QrScannerModal.jsx
import { useEffect, useRef } from 'react';
import {
    Modal, ModalOverlay, ModalContent, ModalHeader,
    ModalCloseButton, ModalBody, Box, Text
} from '@chakra-ui/react';
import { Html5Qrcode } from 'html5-qrcode';

export default function QrScannerModal({ isOpen, onClose, onDetected }) {
    const regionIdRef = useRef(`qr-reader-${Math.random().toString(36).slice(2)}`);
    const scannerRef = useRef(null);
    const startingRef = useRef(false); // guard від подвійного старту

    useEffect(() => {
        if (!isOpen) return;

        let cancelled = false;

        const waitForElement = async (id, tries = 40, delayMs = 50) => {
            for (let i = 0; i < tries; i++) {
                const el = document.getElementById(id);
                if (el) return el;
                await new Promise(r => setTimeout(r, delayMs));
                if (cancelled) return null;
            }
            return null;
        };

        const start = async () => {
            if (startingRef.current) return;
            startingRef.current = true;

            const id = regionIdRef.current;
            const el = await waitForElement(id);
            if (!el || cancelled) {
                startingRef.current = false;
                return;
            }

            const scanner = new Html5Qrcode(id);
            scannerRef.current = scanner;

            try {
                await scanner.start(
                    { facingMode: 'environment' },
                    { fps: 10, qrbox: { width: 250, height: 250 } },
                    (decodedText /* , decodedResult */) => {
                        // як тільки зчитали — віддаємо наверх і закриваємо модалку
                        onDetected?.(decodedText);
                    },
                    () => {} // можна ігнорити помилки кадрів
                );
            } catch (e) {
                console.error('QR start error', e);
            } finally {
                startingRef.current = false;
            }
        };

        start();

        return () => {
            cancelled = true;
            const s = scannerRef.current;
            scannerRef.current = null;
            startingRef.current = false;
            if (s) {
                // акуратно зупиняємо, щоб відпустити камеру
                s.stop()
                    .then(() => s.clear())
                    .catch(() => {});
            }
        };
    }, [isOpen, onDetected]);

    return (
        <Modal isOpen={isOpen} onClose={onClose} size="md" isCentered>
            <ModalOverlay />
            <ModalContent>
                <ModalHeader>Scan ticket QR</ModalHeader>
                <ModalCloseButton />
                <ModalBody>
                    {/* важливо: контейнер існує ДО старту сканера */}
                    <Box id={regionIdRef.current} minH="260px" />
                    <Text fontSize="xs" color="gray.500" mt={2}>
                        Aim the camera at the QR code.
                    </Text>
                </ModalBody>
            </ModalContent>
        </Modal>
    );
}
