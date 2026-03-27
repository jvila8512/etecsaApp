import React, { useEffect, useState } from 'react';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { toast } from 'react-toastify';
import { Box, Button, Container, Paper, Typography } from '@mui/material';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import PasswordStrengthBar from 'app/shared/layout/password/password-strength-bar';
import { reset, savePassword } from './password.reducer';

export const PasswordPage = () => {
  const [password, setPassword] = useState('');
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(reset());
    dispatch(getSession());
    return () => {
      dispatch(reset());
    };
  }, []);

  const handleValidSubmit = ({ currentPassword, newPassword }) => {
    dispatch(savePassword({ currentPassword, newPassword }));
  };

  const updatePassword = event => setPassword(event.target.value);

  const account = useAppSelector(state => state.authentication.account);
  const successMessage = useAppSelector(state => state.password.successMessage);
  const errorMessage = useAppSelector(state => state.password.errorMessage);

  useEffect(() => {
    if (successMessage) {
      toast.success(translate(successMessage));
    } else if (errorMessage) {
      toast.error(translate(errorMessage));
    }
    dispatch(reset());
  }, [successMessage, errorMessage]);

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      <Paper sx={{ p: 4 }}>
        <Typography variant="h5" id="password-title" sx={{ mb: 3, fontWeight: 700 }}>
          <Translate contentKey="password.title" interpolate={{ username: account.login }}>
            Password for {account.login}
          </Translate>
        </Typography>

        <ValidatedForm id="password-form" onSubmit={handleValidSubmit}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <ValidatedField
              name="currentPassword"
              label={translate('global.form.currentpassword.label')}
              placeholder={translate('global.form.currentpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.newpassword.required') },
              }}
              data-cy="currentPassword"
            />
            <ValidatedField
              name="newPassword"
              label={translate('global.form.newpassword.label')}
              placeholder={translate('global.form.newpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.newpassword.required') },
                minLength: { value: 4, message: translate('global.messages.validate.newpassword.minlength') },
                maxLength: { value: 50, message: translate('global.messages.validate.newpassword.maxlength') },
              }}
              onChange={updatePassword}
              data-cy="newPassword"
            />
            <PasswordStrengthBar password={password} />
            <ValidatedField
              name="confirmPassword"
              label={translate('global.form.confirmpassword.label')}
              placeholder={translate('global.form.confirmpassword.placeholder')}
              type="password"
              validate={{
                required: { value: true, message: translate('global.messages.validate.confirmpassword.required') },
                minLength: { value: 4, message: translate('global.messages.validate.confirmpassword.minlength') },
                maxLength: { value: 50, message: translate('global.messages.validate.confirmpassword.maxlength') },
                validate: v => v === password || translate('global.messages.error.dontmatch'),
              }}
              data-cy="confirmPassword"
            />
            <Button variant="contained" color="success" type="submit" data-cy="submit" sx={{ mt: 1 }}>
              <Translate contentKey="password.form.button">Save</Translate>
            </Button>
          </Box>
        </ValidatedForm>
      </Paper>
    </Container>
  );
};

export default PasswordPage;
