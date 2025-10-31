import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin-login.component.html',
  styleUrls: ['./admin-login.component.css']
})
export class AdminLoginComponent {
  email = '';
  motDePasse = '';
  loading = false;
  error: string | null = null;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = null;
    this.loading = true;
    this.auth.loginV1({ email: this.email, motDePasse: this.motDePasse }).subscribe({
      next: (resp) => {
        this.loading = false;
        const user = resp?.user || resp;
        const rawRoles: any = user?.roles ?? user?.role ?? [];
        const roleList: string[] = Array.isArray(rawRoles)
          ? (rawRoles as string[])
          : (typeof rawRoles === 'string' ? rawRoles.split(',') : []);
        const normalized = roleList.map(r => String(r).trim().toUpperCase());
        const isAdmin = normalized.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN');

        if (isAdmin) {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.error = "Accès refusé: cette page est réservée au Super Admin.";
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Échec de la connexion.';
      }
    });
  }
}
