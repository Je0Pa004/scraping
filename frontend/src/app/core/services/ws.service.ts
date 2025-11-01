import { Injectable, NgZone } from '@angular/core';
import { Observable, Subject, timer } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

function httpToWs(url: string): string {
  try {
    const u = new URL(url);
    u.protocol = (u.protocol === 'https:' ? 'wss:' : 'ws:');
    // Default path '/ws' when pointing to API root
    if (!u.pathname || u.pathname === '/' || u.pathname.endsWith('/api')) {
      u.pathname = (u.pathname?.replace(/\/$/, '') || '') + '/ws';
    }
    return u.toString();
  } catch {
    return url.replace(/^http/, 'ws') + '/ws';
  }
}

@Injectable({ providedIn: 'root' })
export class WsService {
  private stop$ = new Subject<void>();

  constructor(private zone: NgZone) {}

  connect(pathOrAbsolute?: string): Observable<any> {
    const base = environment.api || environment.apiV1 || window.location.origin;
    const wsUrl = pathOrAbsolute?.startsWith('ws') || pathOrAbsolute?.startsWith('wss')
      ? (pathOrAbsolute as string)
      : (pathOrAbsolute ? httpToWs(base + pathOrAbsolute) : httpToWs(base));

    const out$ = new Subject<any>();
    let socket: WebSocket | null = null;
    let retries = 0;

    const openSocket = () => {
      try {
        socket = new WebSocket(wsUrl);
        socket.onopen = () => {
          retries = 0;
        };
        socket.onmessage = (evt) => {
          this.zone.run(() => {
            try {
              const data = JSON.parse(evt.data);
              out$.next(data);
            } catch {
              out$.next(evt.data);
            }
          });
        };
        socket.onerror = () => {
          socket?.close();
        };
        socket.onclose = () => {
          socket = null;
          // reconnect with backoff
          retries = Math.min(retries + 1, 6);
          const delayMs = Math.pow(2, retries) * 500;
          timer(delayMs).pipe(takeUntil(this.stop$)).subscribe(() => openSocket());
        };
      } catch {
        retries = Math.min(retries + 1, 6);
        const delayMs = Math.pow(2, retries) * 500;
        timer(delayMs).pipe(takeUntil(this.stop$)).subscribe(() => openSocket());
      }
    };

    openSocket();

    const observable = new Observable<any>((subscriber) => {
      const sub = out$.subscribe(subscriber);
      return () => {
        sub.unsubscribe();
        if (socket && socket.readyState === WebSocket.OPEN) {
          socket.close();
        }
      };
    });

    return observable;
  }

  stop(): void {
    this.stop$.next();
  }
}
