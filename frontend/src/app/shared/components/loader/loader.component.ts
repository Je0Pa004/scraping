import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.css']
})
export class LoaderComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() color: string = '#0d6efd';
  @Input() message: string = 'Loading...';
  @Input() showMessage: boolean = true;
  @Input() overlay: boolean = false;

  get loaderSize(): string {
    const sizes = {
      sm: '2rem',
      md: '3rem',
      lg: '4rem'
    };
    return sizes[this.size];
  }

  get spinnerSize(): string {
    const sizes = {
      sm: '1rem',
      md: '1.5rem',
      lg: '2rem'
    };
    return sizes[this.size];
  }
}
