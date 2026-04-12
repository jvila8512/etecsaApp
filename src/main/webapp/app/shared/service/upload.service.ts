import axios from 'axios';

const API_URI = 'api/uploads';

export interface UploadResponse {
  url: string;
  error?: string;
}

export interface UploadConfig {
  folders: string[];
  maxSizePerFolder: Record<string, number>;
  root: string;
}

/**
 * Upload a file to a specific folder.
 * @param file - The file to upload
 * @param folder - The target folder (profile, alarmas, equipos, eventos, documentos)
 */
export const uploadFile = async (file: File, folder: string): Promise<UploadResponse> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);

  const response = await axios.post<UploadResponse>(API_URI, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};

/**
 * Delete a file.
 * @param folder - The folder name
 * @param filename - The filename to delete
 */
export const deleteFile = async (folder: string, filename: string): Promise<void> => {
  await axios.delete(`${API_URI}/${folder}/${filename}`);
};

/**
 * Get upload configuration.
 */
export const getUploadConfig = async (): Promise<UploadConfig> => {
  const response = await axios.get<UploadConfig>(`${API_URI}/config`);
  return response.data;
};

/**
 * Get full URL for a file.
 */
export const getFileUrl = (relativeUrl: string): string => {
  if (!relativeUrl) return '';

  // If it's already a full URL, return as is
  if (relativeUrl.startsWith('http://') || relativeUrl.startsWith('https://') || relativeUrl.startsWith('data:')) {
    return relativeUrl;
  }

  // If it's a relative URL starting with /api/, prepend the base URL
  if (relativeUrl.startsWith('/api/')) {
    return relativeUrl;
  }

  return `/api/uploads/${relativeUrl}`;
};
