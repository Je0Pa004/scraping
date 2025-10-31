import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Language {
  code: string;
  name: string;
  flag: string;
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private currentLanguageSubject = new BehaviorSubject<string>('fr');
  public currentLanguage$ = this.currentLanguageSubject.asObservable();

  private languages: Language[] = [
    { code: 'fr', name: 'Français', flag: '🇫🇷' },
    { code: 'en', name: 'English', flag: '🇬🇧' }
  ];

  private translations: { [key: string]: { [key: string]: string } } = {
    fr: {
      // Navigation
      'nav.dashboard': 'Tableau de Bord',
      'nav.search': 'Recherche',
      'nav.results': 'Résultats',
      'nav.subscriptions': 'Abonnements',
      'nav.profile': 'Profil',
      'nav.admin': 'Administrateur',
      'nav.logout': 'Déconnexion',

      // Common
      'common.save': 'Enregistrer',
      'common.cancel': 'Annuler',
      'common.delete': 'Supprimer',
      'common.edit': 'Modifier',
      'common.create': 'Créer',
      'common.loading': 'Chargement...',
      'common.error': 'Erreur',
      'common.success': 'Succès',

      // Admin
      'admin.title': 'Tableau de Bord Administrateur',
      'admin.users': 'Gestion des Utilisateurs',
      'admin.subscriptions': 'Gestion des Abonnements',
      'admin.dashboard': 'Tableau de Bord Administrateur',

      // User Management
      'users.title': 'Gestion des Utilisateurs',
      'users.create': 'Créer un Utilisateur',
      'users.search': 'Rechercher par nom ou email...',
      'users.all_status': 'Tous les statuts',
      'users.active': 'Actif',
      'users.inactive': 'Inactif',
      'users.suspended': 'Suspendu',
      'users.username': 'Nom d\'utilisateur',
      'users.email': 'Email',
      'users.role': 'Rôle',
      'users.status': 'Statut',
      'users.created_date': 'Date de création',
      'users.actions': 'Actions',
      'users.admin': 'Administrateur',
      'users.user': 'Utilisateur',
      'users.confirm_delete': 'Êtes-vous sûr de vouloir supprimer cet utilisateur ?',

      // Subscription Management
      'subscriptions.title': 'Abonnements',
      'subscriptions.description': 'Gérez votre forfait et vos limites',
      'subscriptions.current_usage': 'Utilisation actuelle',
      'subscriptions.current_plan': 'Plan actuel',
      'subscriptions.searches_used': 'recherches utilisées',
      'subscriptions.cancel_subscription': 'Annuler l\'abonnement',
      'subscriptions.renewal_date': 'Renouvellement le',
      'subscriptions.choose_plan': 'Choisissez votre plan',
      'subscriptions.searches_per_month': 'recherches par mois',
      'subscriptions.max_profiles': 'profils max',
      'subscriptions.faq': 'Questions fréquentes',
      'subscriptions.faq_question_1': 'Puis-je changer de plan à tout moment?',
      'subscriptions.faq_answer_1': 'Oui, vous pouvez upgrader ou downgrader votre plan à tout moment. Les changements sont appliqués immédiatement.',
      'subscriptions.faq_question_2': 'Que se passe-t-il si je dépasse ma limite?',
      'subscriptions.faq_answer_2': 'Vous serez notifié lorsque vous approchez de votre limite. Vous pouvez upgrader votre plan ou attendre le prochain cycle.',
      'subscriptions.faq_question_3': 'Les données sont-elles conservées si je change de plan?',
      'subscriptions.faq_answer_3': 'Oui, toutes vos recherches et données sont conservées, quel que soit votre plan.',

      // Auth
      'auth.login': 'Se connecter',
      'auth.register': 'S\'inscrire',
      'auth.email': 'Email',
      'auth.password': 'Mot de passe',
      'auth.confirm_password': 'Confirmer le mot de passe',
      'auth.forgot_password': 'Mot de passe oublié ?',
      'auth.no_account': 'Pas encore de compte ?',
      'auth.have_account': 'Déjà un compte ?',
    },
    en: {
      // Navigation
      'nav.dashboard': 'Dashboard',
      'nav.search': 'Search',
      'nav.results': 'Results',
      'nav.subscriptions': 'Subscriptions',
      'nav.profile': 'Profile',
      'nav.admin': 'Admin',
      'nav.logout': 'Logout',

      // Common
      'common.save': 'Save',
      'common.cancel': 'Cancel',
      'common.delete': 'Delete',
      'common.edit': 'Edit',
      'common.create': 'Create',
      'common.loading': 'Loading...',
      'common.error': 'Error',
      'common.success': 'Success',

      // Admin
      'admin.title': 'Admin Dashboard',
      'admin.users': 'User Management',
      'admin.subscriptions': 'Subscription Management',
      'admin.dashboard': 'Admin Dashboard',

      // User Management
      'users.title': 'User Management',
      'users.create': 'Create User',
      'users.search': 'Search by name or email...',
      'users.all_status': 'All statuses',
      'users.active': 'Active',
      'users.inactive': 'Inactive',
      'users.suspended': 'Suspended',
      'users.username': 'Username',
      'users.email': 'Email',
      'users.role': 'Role',
      'users.status': 'Status',
      'users.created_date': 'Creation Date',
      'users.actions': 'Actions',
      'users.admin': 'Administrator',
      'users.user': 'User',
      'users.confirm_delete': 'Are you sure you want to delete this user?',

      // Subscription Management
      'subscriptions.title': 'Subscriptions',
      'subscriptions.description': 'Manage your plan and limits',
      'subscriptions.current_usage': 'Current Usage',
      'subscriptions.current_plan': 'Current Plan',
      'subscriptions.searches_used': 'searches used',
      'subscriptions.cancel_subscription': 'Cancel Subscription',
      'subscriptions.renewal_date': 'Renewal on',
      'subscriptions.choose_plan': 'Choose Plan',
      'subscriptions.searches_per_month': 'searches per month',
      'subscriptions.max_profiles': 'max profiles',
      'subscriptions.faq': 'Frequently Asked Questions',
      'subscriptions.faq_question_1': 'Can I change my plan at any time?',
      'subscriptions.faq_answer_1': 'Yes, you can upgrade or downgrade your plan at any time. Changes are applied immediately.',
      'subscriptions.faq_question_2': 'What happens if I exceed my limit?',
      'subscriptions.faq_answer_2': 'You will be notified when you approach your limit. You can upgrade your plan or wait for the next cycle.',
      'subscriptions.faq_question_3': 'Are data retained if I change plans?',
      'subscriptions.faq_answer_3': 'Yes, all your searches and data are retained regardless of your plan.',

      // Auth
      'auth.login': 'Login',
      'auth.register': 'Register',
      'auth.email': 'Email',
      'auth.password': 'Password',
      'auth.confirm_password': 'Confirm Password',
      'auth.forgot_password': 'Forgot password?',
      'auth.no_account': 'Don\'t have an account?',
      'auth.have_account': 'Already have an account?',
    }
  };

  constructor() {
    // Load saved language from localStorage
    const savedLanguage = localStorage.getItem('language');
    if (savedLanguage && this.languages.some(lang => lang.code === savedLanguage)) {
      this.currentLanguageSubject.next(savedLanguage);
    }
  }

  getCurrentLanguage(): string {
    return this.currentLanguageSubject.value;
  }

  getLanguages(): Language[] {
    return this.languages;
  }

  switchLanguage(languageCode: string): void {
    if (this.languages.some(lang => lang.code === languageCode)) {
      this.currentLanguageSubject.next(languageCode);
      localStorage.setItem('language', languageCode);
    }
  }

  translate(key: string): string {
    const currentLang = this.getCurrentLanguage();
    return this.translations[currentLang]?.[key] || key;
  }

  getTranslation(key: string, lang?: string): string {
    const language = lang || this.getCurrentLanguage();
    return this.translations[language]?.[key] || key;
  }
}
