import React, { useEffect, useRef } from 'react';

interface AlarmSoundProps {
  active: boolean;
}

const AlarmSound: React.FC<AlarmSoundProps> = ({ active }) => {
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    audioRef.current = new Audio('/content/sonidos/Alarma 1.mp3');
    audioRef.current.loop = true;

    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  useEffect(() => {
    if (audioRef.current) {
      if (active) {
        audioRef.current.play().catch(() => {});
      } else {
        audioRef.current.pause();
        audioRef.current.currentTime = 0;
      }
    }
  }, [active]);

  return null;
};

export default AlarmSound;
