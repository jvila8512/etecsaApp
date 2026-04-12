import React, { useEffect, useRef, useState } from 'react';
import { translate } from 'react-jhipster';
import { toast } from 'react-toastify';
import { Box, Button, Container, Paper, Typography, Avatar, TextField, MenuItem, CircularProgress, InputAdornment } from '@mui/material';
import PhotoCameraIcon from '@mui/icons-material/PhotoCamera';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonIcon from '@mui/icons-material/Person';
import EmailIcon from '@mui/icons-material/Email';
import PersonPinIcon from '@mui/icons-material/PersonPin';
import LanguageIcon from '@mui/icons-material/Language';
import SaveIcon from '@mui/icons-material/Save';

import { languages, locales } from 'app/config/translation';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import { saveAccountSettings } from './settings.reducer';
import { uploadFile, deleteFile } from 'app/shared/service/upload.service';

export const SettingsPage = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const successMessage = useAppSelector(state => state.settings.successMessage);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    langKey: 'es',
  });
  const [saving, setSaving] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState('');

  useEffect(() => {
    dispatch(getSession());
  }, [dispatch]);

  useEffect(() => {
    if (account) {
      setFormData({
        firstName: account.firstName || '',
        lastName: account.lastName || '',
        email: account.email || '',
        langKey: account.langKey || 'es',
      });
      setPreviewUrl(account.imageUrl || '');
    }
  }, [account]);

  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
    }
  }, [successMessage]);

  // Watch for success to unlock button
  useEffect(() => {
    if (!successMessage && !saving) {
      // Previous save completed
    }
  }, [successMessage, saving]);

  const handleInputChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [field]: event.target.value });
  };

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    setSaving(true);
    dispatch(
      saveAccountSettings({
        ...account,
        ...formData,
      }),
    );
    // Delay to allow dispatch to complete
    setTimeout(() => {
      setSaving(false);
    }, 500);
  };

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      toast.error('Tipo no válido. Solo JPEG, PNG, GIF o WebP');
      return;
    }

    if (file.size > 500_000) {
      toast.error('Archivo muy grande. Máximo 500KB');
      return;
    }

    const reader = new FileReader();
    reader.onload = e => setPreviewUrl(e.target?.result as string);
    reader.readAsDataURL(file);

    setUploading(true);
    try {
      const oldImageUrl = account.imageUrl;
      if (oldImageUrl) {
        try {
          const oldFilename = oldImageUrl.split('/').pop();
          if (oldFilename) await deleteFile('profile', oldFilename);
        } catch {
          // Ignore
        }
      }

      const response = await uploadFile(file, 'profile');
      if (response.error) {
        toast.error(response.error);
        setPreviewUrl(account.imageUrl || '');
      } else {
        dispatch(saveAccountSettings({ ...account, imageUrl: response.url }));
        toast.success('Foto de perfil actualizada');
      }
    } catch (error) {
      toast.error('Error al subir la imagen');
    } finally {
      setUploading(false);
    }
  };

  const handleDeleteImage = async () => {
    if (!account.imageUrl) return;
    try {
      const filename = account.imageUrl.split('/').pop();
      if (filename) await deleteFile('profile', filename);
      setPreviewUrl('');
      dispatch(saveAccountSettings({ ...account, imageUrl: '' }));
      toast.success('Foto eliminada');
    } catch {
      toast.error('Error al eliminar la imagen');
    }
  };

  const getInitials = () => {
    const first = account?.firstName?.[0] || '';
    const last = account?.lastName?.[0] || '';
    return (first + last).toUpperCase() || account?.login?.[0]?.toUpperCase() || '?';
  };

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Paper sx={{ p: 4, borderRadius: 2 }}>
        <Typography variant="h5" sx={{ mb: 3, fontWeight: 700, color: '#1e293b' }}>
          Configuración de Usuario
        </Typography>

        {/* Foto de perfil */}
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
          <Box sx={{ position: 'relative' }}>
            {uploading && (
              <Box
                sx={{
                  position: 'absolute',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  bgcolor: 'rgba(255,255,255,0.9)',
                  width: 100,
                  height: 100,
                  borderRadius: '50%',
                  zIndex: 1,
                }}
              >
                <CircularProgress size={30} />
              </Box>
            )}
            {previewUrl ? (
              <Avatar src={previewUrl} sx={{ width: 100, height: 100, border: '3px solid #e2e8f0' }} />
            ) : (
              <Avatar sx={{ width: 100, height: 100, bgcolor: '#2563eb', fontSize: '2.5rem' }}>{getInitials()}</Avatar>
            )}
          </Box>

          <input
            type="file"
            ref={fileInputRef}
            accept="image/jpeg,image/png,image/gif,image/webp"
            onChange={handleFileSelect}
            style={{ display: 'none' }}
          />

          <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
            <Button
              variant="contained"
              size="small"
              startIcon={<PhotoCameraIcon />}
              onClick={() => fileInputRef.current?.click()}
              disabled={uploading}
              sx={{ bgcolor: '#2563eb', '&:hover': { bgcolor: '#1d4ed8' } }}
            >
              Cambiar
            </Button>
            {previewUrl && (
              <Button
                variant="outlined"
                size="small"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={handleDeleteImage}
                disabled={uploading}
              >
                Eliminar
              </Button>
            )}
          </Box>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1 }}>
            Máx 500KB • JPEG, PNG, GIF o WebP
          </Typography>
        </Box>

        {/* Formulario */}
        <Box component="form" onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Usuario"
            value={account.login || ''}
            disabled
            sx={{ mb: 2 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <PersonIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
          />

          <TextField
            fullWidth
            label="Nombre"
            value={formData.firstName}
            onChange={handleInputChange('firstName')}
            required
            sx={{ mb: 2 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <PersonPinIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
          />

          <TextField
            fullWidth
            label="Apellidos"
            value={formData.lastName}
            onChange={handleInputChange('lastName')}
            required
            sx={{ mb: 2 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <PersonPinIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
          />

          <TextField
            fullWidth
            label="Correo electrónico"
            value={formData.email}
            onChange={handleInputChange('email')}
            required
            type="email"
            sx={{ mb: 2 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <EmailIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
          />

          <TextField
            fullWidth
            select
            label="Idioma"
            value={formData.langKey}
            onChange={handleInputChange('langKey')}
            sx={{ mb: 3 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LanguageIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
          >
            {locales.map(locale => (
              <MenuItem value={locale} key={locale}>
                {languages[locale].name}
              </MenuItem>
            ))}
          </TextField>

          <Button
            type="submit"
            fullWidth
            variant="contained"
            disabled={saving}
            startIcon={saving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
            sx={{
              bgcolor: '#22c55e',
              '&:hover': { bgcolor: '#16a34a' },
              py: 1.5,
              fontWeight: 600,
            }}
          >
            {saving ? 'Guardando...' : 'Guardar cambios'}
          </Button>
        </Box>
      </Paper>
    </Container>
  );
};

export default SettingsPage;
