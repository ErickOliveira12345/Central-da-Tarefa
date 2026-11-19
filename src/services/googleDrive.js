
import { registerPlugin } from '@capacitor/core';

const GoogleDrive = registerPlugin('GoogleDrive');

export async function autorizarGoogleDrive() {
  const result = await GoogleDrive.authorize();

  if (!result?.accessToken) {
    throw new Error('Não foi possível obter autorização do Google Drive.');
  }

  return result.accessToken;
}
