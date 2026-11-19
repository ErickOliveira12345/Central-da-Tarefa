

import React, { useState } from 'react';
import {
  exportBackup,
  importBackup
} from '../db/indexedDB';
import { useData } from '../context/DataContext';
import { Capacitor } from '@capacitor/core';
import { autorizarGoogleDrive } from '../services/googleDrive';

export default function Backup() {
  const [message, setMessage] = useState('');
  const [authLoading, setAuthLoading] = useState(false);

  const { refresh } = useData();

  // Testar a autorização do Google Drive.
  async function connectGoogleDrive() {
    if (!Capacitor.isNativePlatform()) {
      setMessage(
        'A autorização do Google Drive está disponível no aplicativo Android.'
      );
      return;
    }

    try {
      setAuthLoading(true);
      setMessage('');

      const accessToken = await autorizarGoogleDrive();

      if (!accessToken) {
        throw new Error('Não foi possível obter o token do Google.');
      }

      setMessage(
        'Autorização Google concedida! Nenhum arquivo foi enviado ainda.'
      );
    } catch (error) {
      setMessage(
        'Não foi possível autorizar: ' + error.message
      );
    } finally {
      setAuthLoading(false);
    }
  }

  // Exportar os dados do sistema para um arquivo JSON.
  async function download() {
    try {
      const json = JSON.stringify(
        await exportBackup(),
        null,
        2
      );

      const url = URL.createObjectURL(
        new Blob(
          [json],
          { type: 'application/json' }
        )
      );

      const a = document.createElement('a');
      a.href = url;

      a.download =
        'gerencial-backup-' +
        new Date().toISOString().slice(0, 10) +
        '.json';

      a.click();

      setTimeout(() => URL.revokeObjectURL(url), 1000);

      setMessage(
        'Backup gerado. Guarde o arquivo em local seguro.'
      );
    } catch (e) {
      setMessage(e.message);
    }
  }

  // Restaurar os dados a partir de um arquivo JSON.
  async function restore(e) {
    const file = e.target.files?.[0];

    if (!file) return;

    if (
      !window.confirm(
        'Restaurar substitui TODOS os dados atuais. Deseja continuar?'
      )
    ) {
      e.target.value = '';
      return;
    }

    try {
      const json = JSON.parse(await file.text());

      await importBackup(json);
      await refresh();

      setMessage('Backup restaurado.');
    } catch (err) {
      setMessage('Erro: ' + err.message);
    }

    e.target.value = '';
  }

  return (
    <>
      <header className="page-header">
        <div>
          <h1>Backup</h1>
          <p>
            Proteja os dados armazenados neste dispositivo
          </p>
        </div>
      </header>

      {/* Backup online no Google Drive */}
      <section className="panel">
        <h2>Backup online</h2>

        <p>
          Autorize o acesso à sua conta Google para preparar
          o backup automático no seu Google Drive.
        </p>

        <button
          className="primary"
          onClick={connectGoogleDrive}
          disabled={authLoading}
        >
          {authLoading
            ? 'Aguardando autorização...'
            : 'Conectar ao Google Drive'}
        </button>
      </section>

      {/* Exportação manual */}
      <section className="panel">
        <h2>Exportar dados</h2>

        <p>
          Salve uma cópia JSON fora do navegador.
        </p>

        <button
          className="primary"
          onClick={download}
        >
          Baixar backup
        </button>
      </section>

      {/* Restauração manual */}
      <section className="panel">
        <h2>Restaurar dados</h2>

        <p>
          Atenção: a restauração substitui todos os cadastros atuais.
        </p>

        <input
          aria-label="Selecionar arquivo de backup"
          type="file"
          accept=".json,application/json"
          onChange={restore}
        />
      </section>

      {message && (
        <p role="status">
          {message}
        </p>
      )}
    </>
  );
}
