import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { CandidatsService } from '../../../core/services/candidats.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-upload-cv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload-cv.component.html',
  styleUrls: ['./upload-cv.component.css']
})
export class UploadCvComponent {
  token: string | null = null;
  file: File | null = null;
  uploading = false;

  constructor(private route: ActivatedRoute, private candidats: CandidatsService, private notify: NotificationService) {
    this.route.queryParamMap.subscribe(p => {
      this.token = p.get('token');
    });
  }

  onFileChange(event: any) {
    const f = event?.target?.files?.[0];
    this.file = f || null;
  }

  submit() {
    if (!this.token) {
      this.notify.showError('Lien invalide ou expiré');
      return;
    }
    if (!this.file) {
      this.notify.showWarning('Veuillez sélectionner un fichier');
      return;
    }
    this.uploading = true;
    this.candidats.uploadCv(this.token, this.file).subscribe({
      next: () => {
        this.uploading = false;
        this.notify.showSuccess('CV reçu, merci !');
      },
      error: () => {
        this.uploading = false;
        this.notify.showError('Échec de l\'upload du CV');
      }
    });
  }
}
