import React, { useState } from 'react';
import { Translate, ValidatedField, translate } from 'react-jhipster';
import { Link } from 'react-router-dom';
import { type FieldError, useForm } from 'react-hook-form';
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography,
  Link as MuiLink,
  CircularProgress,
} from '@mui/material';
import LockResetIcon from '@mui/icons-material/LockReset';
import HowToRegIcon from '@mui/icons-material/HowToReg';
import LoginIcon from '@mui/icons-material/Login';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const [loading, setLoading] = useState(false);

  const login = ({ username, password, rememberMe }) => {
    setLoading(true);
    props.handleLogin(username, password, rememberMe);
    // Reset loading after a timeout in case the redirect doesn't happen
    setTimeout(() => setLoading(false), 3000);
  };

  const {
    handleSubmit,
    register,
    formState: { errors, touchedFields },
  } = useForm({ mode: 'onTouched' });

  const { loginError, handleClose } = props;

  const handleLoginSubmit = e => {
    handleSubmit(login)(e);
  };

  return (
    <Dialog open={props.showModal} onClose={handleClose} maxWidth="xs" fullWidth>
      <form onSubmit={handleLoginSubmit}>
        {/* Logo y Título */}
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', pt: 3, pb: 1 }}>
          <Box component="img" src="content/images/logotipo-etecsa.jpg" alt="ETECSA" sx={{ height: 60, objectFit: 'contain', mb: 2 }} />
          <DialogTitle id="login-title" data-cy="loginTitle" sx={{ textAlign: 'center', p: 0, fontWeight: 700, fontSize: '1.3rem' }}>
            Sistema de Supervisión y Control
          </DialogTitle>
        </Box>

        <DialogContent>
          {loginError ? (
            <Alert severity="error" data-cy="loginError" sx={{ mb: 2 }}>
              <Translate contentKey="login.messages.error.authentication">
                <strong>Failed to sign in!</strong> Please check your credentials and try again.
              </Translate>
            </Alert>
          ) : null}

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <ValidatedField
              name="username"
              label={translate('global.form.username.label')}
              placeholder={translate('global.form.username.placeholder')}
              required
              autoFocus
              data-cy="username"
              validate={{ required: 'Username cannot be empty!' }}
              register={register}
              error={errors.username as FieldError}
              isTouched={touchedFields.username}
            />
            <ValidatedField
              name="password"
              type="password"
              label={translate('login.form.password')}
              placeholder={translate('login.form.password.placeholder')}
              required
              data-cy="password"
              validate={{ required: 'Password cannot be empty!' }}
              register={register}
              error={errors.password as FieldError}
              isTouched={touchedFields.password}
            />
            <ValidatedField
              name="rememberMe"
              type="checkbox"
              check
              label={translate('login.form.rememberme')}
              value={true}
              register={register}
            />
          </Box>

          {/* Olvidaste tu contraseña */}
          <Alert severity="info" icon={<LockResetIcon />} sx={{ mt: 2 }}>
            <MuiLink
              component={Link}
              to="/account/reset/request"
              data-cy="forgetYourPasswordSelector"
              underline="hover"
              sx={{ fontWeight: 500 }}
            >
              <Translate contentKey="login.password.forgot">Did you forget your password?</Translate>
            </MuiLink>
          </Alert>

          {/* Crear cuenta */}
          <Alert severity="info" icon={<HowToRegIcon />} sx={{ mt: 1 }}>
            <span>
              <Translate contentKey="global.messages.info.register.noaccount">You don&apo;t have an account yet?</Translate>
            </span>{' '}
            <MuiLink component={Link} to="/account/register" underline="hover" sx={{ fontWeight: 500 }}>
              <Translate contentKey="global.messages.info.register.link">Register a new account</Translate>
            </MuiLink>
          </Alert>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={handleClose} color="inherit">
            <Translate contentKey="entity.action.cancel">Cancel</Translate>
          </Button>
          <Button
            type="submit"
            variant="contained"
            data-cy="submit"
            disabled={loading}
            startIcon={loading ? <CircularProgress size={18} color="inherit" /> : <LoginIcon />}
          >
            {loading ? 'Conectando...' : <Translate contentKey="login.form.button">Sign in</Translate>}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
};

export default LoginModal;
