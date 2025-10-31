import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  timestamp: Date;
  read: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notifications = new BehaviorSubject<Notification[]>([]);
  public notifications$ = this.notifications.asObservable();

  constructor() {}

  showSuccess(message: string, title: string = 'Success') {
    this.addNotification({
      id: this.generateId(),
      type: 'success',
      title,
      message,
      timestamp: new Date(),
      read: false
    });
  }

  showError(message: string, title: string = 'Error') {
    this.addNotification({
      id: this.generateId(),
      type: 'error',
      title,
      message,
      timestamp: new Date(),
      read: false
    });
  }

  showWarning(message: string, title: string = 'Warning') {
    this.addNotification({
      id: this.generateId(),
      type: 'warning',
      title,
      message,
      timestamp: new Date(),
      read: false
    });
  }

  showInfo(message: string, title: string = 'Info') {
    this.addNotification({
      id: this.generateId(),
      type: 'info',
      title,
      message,
      timestamp: new Date(),
      read: false
    });
  }

  private addNotification(notification: Notification) {
    const currentNotifications = this.notifications.value;
    this.notifications.next([notification, ...currentNotifications]);

    // Auto-remove after 5 seconds
    setTimeout(() => {
      this.removeNotification(notification.id);
    }, 5000);
  }

  removeNotification(id: string) {
    const currentNotifications = this.notifications.value;
    this.notifications.next(currentNotifications.filter(n => n.id !== id));
  }

  markAsRead(id: string) {
    const currentNotifications = this.notifications.value;
    const updatedNotifications = currentNotifications.map(n =>
      n.id === id ? { ...n, read: true } : n
    );
    this.notifications.next(updatedNotifications);
  }

  clearAll() {
    this.notifications.next([]);
  }

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }
}
