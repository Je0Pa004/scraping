import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent implements OnInit {
  @Input() type: 'success' | 'error' | 'warning' | 'info' = 'info';
  @Input() message: string = '';
  @Input() title: string = '';
  @Input() dismissible: boolean = true;
  @Input() autoClose: boolean = false;
  @Input() autoCloseDelay: number = 5000;

  @Output() close = new EventEmitter<void>();

  show: boolean = true;
  private autoCloseTimeout: any;

  ngOnInit(): void {
    if (this.autoClose && this.autoCloseDelay > 0) {
      this.autoCloseTimeout = setTimeout(() => {
        this.onClose();
      }, this.autoCloseDelay);
    }
  }

  ngOnDestroy(): void {
    if (this.autoCloseTimeout) {
      clearTimeout(this.autoCloseTimeout);
    }
  }

  get alertClass(): string {
    const baseClass = 'alert';
    const typeClass = `alert-${this.type}`;
    const dismissibleClass = this.dismissible ? 'alert-dismissible' : '';
    return `${baseClass} ${typeClass} ${dismissibleClass}`.trim();
  }

  get iconClass(): string {
    const icons = {
      success: 'fas fa-check-circle',
      error: 'fas fa-exclamation-triangle',
      warning: 'fas fa-exclamation-circle',
      info: 'fas fa-info-circle'
    };
    return icons[this.type];
  }

  onClose(): void {
    this.show = false;
    this.close.emit();
  }
}
