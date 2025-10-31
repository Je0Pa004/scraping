import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.css']
})
export class ModalComponent {
  @Input() title: string = '';
  @Input() size: 'sm' | 'md' | 'lg' | 'xl' = 'md';
  @Input() show: boolean = false;
  @Input() closable: boolean = true;
  @Input() showFooter: boolean = true;

  @Output() close = new EventEmitter<void>();
  @Output() confirm = new EventEmitter<void>();

  get modalSizeClass(): string {
    const sizeClasses = {
      sm: 'modal-sm',
      md: '',
      lg: 'modal-lg',
      xl: 'modal-xl'
    };
    return sizeClasses[this.size];
  }

  onClose(): void {
    if (this.closable) {
      this.close.emit();
    }
  }

  onConfirm(): void {
    this.confirm.emit();
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget && this.closable) {
      this.onClose();
    }
  }
}
