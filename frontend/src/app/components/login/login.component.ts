import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  email = '';
  motDePasse = '';
  loading = false;
  error: string | null = null;
  showPassword = false;

  constructor(private auth: AuthService, private router: Router) {}

  submit() {
    this.error = null;
    this.loading = true;
    // Always start with a clean session before new login
    try {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    } catch {}
    this.auth.loginV1({ email: this.email, motDePasse: this.motDePasse }).subscribe({
      next: (response) => {
        this.loading = false;
        console.log('Login response:', response); // Debug
        
        // Rediriger vers la page admin si l'utilisateur est ADMIN, sinon vers le dashboard user
        const user = response?.user || response;
        console.log('User object:', user); // Debug
        console.log('User role:', user?.role, 'User roles:', user?.roles); // Debug

        // Normaliser les rôles (array ou chaîne "ROLE_ADMIN,ROLE_USER")
        const rawRoles: any = user?.roles ?? user?.role ?? [];
        const roleList: string[] = Array.isArray(rawRoles)
          ? rawRoles as string[]
          : (typeof rawRoles === 'string' ? rawRoles.split(',') : []);

        const normalized = roleList
          .map(r => String(r).trim())
          .filter(Boolean)
          .map(r => r.toUpperCase());

        const hasAdmin = normalized.some(r => r === 'ROLE_ADMIN' || r === 'ADMIN');
        const isAdmin = hasAdmin;

        if (isAdmin) {
          console.log('Redirecting to admin dashboard');
          this.router.navigate(['/admin/dashboard']);
          return;
        }
        // Unified: all non-admin users go to dashboard; offers are accessible from sidebar
        this.router.navigate(['/app/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error?.message || 'Échec de la connexion.';
      }
    });
  }
}
