import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError, switchMap } from 'rxjs/operators';
import { Profile } from '../models/profile.model';
import { SearchCriteria } from '../models/search.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ScrapingService {
  private readonly api = environment.api;

  constructor(private http: HttpClient) {}

  performScraping(searchRequest: any): Observable<any> {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const utilisateurPublicId = user?.id || user?.user?.id || user?.publicId || user?.user?.publicId;

    const payload = {
      utilisateurPublicId,
      source: searchRequest?.source || 'GOOGLE_MAP',
      titre: searchRequest?.title || searchRequest?.keywords || (Array.isArray(searchRequest?.keywords) ? searchRequest.keywords[0] : '') || '',
      secteur: searchRequest?.industry || '',
      localisation: searchRequest?.location || '',
      entreprise: searchRequest?.company || '',
      emploi: searchRequest?.jobType || (Array.isArray(searchRequest?.keywords) ? searchRequest.keywords[0] : '') || '',
      tailleEntreprise: searchRequest?.companySize || '',
      maxResults: searchRequest?.maxResults || searchRequest?.maxresults || 5000
    };

    return this.http.post(`${this.api}/scrapings/perform`, payload).pipe(
      map((res: any) => ({ ...res, id: res?.publicId || res?.id }))
    );
  }

  getScrapings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/scrapings`).pipe(
      map((items: any[]) => (items || []).map((s: any) => ({
        ...s,
        id: s.publicId,
        numericId: s.id,
        createdAt: s.dateDemande,
        status: s.statut === 'TERMINE' ? 'completed' : (s.statut === 'EN_COURS' ? 'processing' : 'failed'),
        profilesCount: s.nombreProfilScrape,
        criteria: {
          location: s.localisation,
          keywords: s.titre,
          company: s.entreprise
        }
      })))
    );
  }

  // Delete a scraping by its publicId (resolve numeric id first)
  deleteScrapingByPublicId(publicId: string): Observable<void> {
    // Try direct delete using publicId endpoint
    return this.http.delete<void>(`${this.api}/scrapings/public/${publicId}`).pipe(
      catchError(() => this.http.delete<void>(`${environment.apiV1}/scrapings/public/${publicId}`)),
      catchError(() =>
        // Fallback: resolve numeric id then delete
        this.http.get<any[]>(`${this.api}/scrapings`).pipe(
          map((items: any[]) => (items || []).find(s => `${s.publicId}` === `${publicId}`)),
          map((found: any) => found?.id),
          switchMap((id: any) => {
            if (!id) throw new Error('Scraping not found');
            return this.http.delete<void>(`${this.api}/scrapings/${id}`).pipe(
              catchError(() => this.http.delete<void>(`${environment.apiV1}/scrapings/${id}`))
            );
          })
        )
      )
    );
  }

  deleteScrapingById(id: number | string): Observable<void> {
    return this.http.delete<void>(`${this.api}/scrapings/${id}`).pipe(
      catchError(() => this.http.delete<void>(`${environment.apiV1}/scrapings/${id}`))
    );
  }

  getProfilesByScraping(scrapingId: string): Observable<Profile[]> {
    return this.http.get<any[]>(`${this.api}/profil-scrapers`).pipe(
      map((items: any[]) => (items || []).filter(p => `${p.scrapingPublicId}` === `${scrapingId}`)),
      map((items: any[]) => items.map(p => this.mapProfilScraperToProfile(p)))
    );
  }

  // Raw ProfilScraperResponse[] for results view (nom, description, email, telephone, urlSource, statut)
  getRawProfilesByScraping(scrapingId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/profil-scrapers`).pipe(
      map((items: any[]) => (items || []).filter(p => `${p.scrapingPublicId}` === `${scrapingId}`))
    );
  }

  // All raw profiles across all scrapings
  getAllRawProfiles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.api}/profil-scrapers`);
  }

  // Delete by publicId: resolve numeric id then delete
  deleteProfileByPublicId(publicId: string): Observable<void> {
    return this.http.get<any[]>(`${this.api}/profil-scrapers`).pipe(
      map((items: any[]) => (items || []).find(p => `${p.publicId}` === `${publicId}`)),
      map((found: any) => found?.id),
      switchMap((id: any) => {
        if (!id) {
          throw new Error('Profile not found');
        }
        return this.http.delete<void>(`${this.api}/profil-scrapers/${id}`);
      })
    );
  }

  private mapProfilScraperToProfile(p: any): Profile {
    const name = p.nom || '';
    const [firstName, ...rest] = name.split(' ');
    const lastName = rest.join(' ');
    return {
      id: 0,
      userId: 0,
      firstName: firstName || name,
      lastName: lastName || '',
      phone: p.telephone || '',
      address: '',
      city: '',
      country: '',
      linkedinUrl: p.urlSource || '',
      githubUrl: '',
      portfolioUrl: '',
      bio: p.description || '',
      skills: [],
      experience: [] as any,
      education: [] as any,
      createdAt: new Date(),
      updatedAt: new Date(),
      title: p.description || '',
      location: '',
      company: ''
    } as any;
  }

  getAllProfiles(): Observable<Profile[]> {
    return this.http.get<Profile[]>(`${this.api}/profil-scrapers`);
  }
}
