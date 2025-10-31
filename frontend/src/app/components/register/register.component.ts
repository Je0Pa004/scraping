import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  email = '';
  motDePasse = '';
  loading = false;
  nomComplet = '';
  showPassword = false;
  error: string | null = null;

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    // New registration must start with a clean session
    this.auth.clearSession();
  }

  submit() {
    this.error = null;
    
    // Validation du mot de passe
    if (!this.motDePasse || this.motDePasse.length < 6) {
      this.error = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }
    
    this.loading = true;
    const payload = {
      nom: this.nomComplet,  // Le backend attend 'nom' pas 'nomComplet'
      email: this.email,
      motDePasse: this.motDePasse
    } as const;
    this.auth.registerV1(payload).subscribe({
      next: () => {
        this.loading = false;
        // Ensure previous session is cleared before redirecting to login
        this.auth.clearSession();
        // Rediriger vers la page de connexion après inscription réussie
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading = false;
        const status = (err as any)?.status;
        if (status === 409) {
          this.error = "Cet email est déjà utilisé. Veuillez en choisir un autre.";
        } else {
          this.error = (err as any)?.error?.message || "Échec de l'inscription.";
        }
      }
    });
  }
}
