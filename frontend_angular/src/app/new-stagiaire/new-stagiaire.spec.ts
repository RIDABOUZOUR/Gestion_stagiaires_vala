import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NewStagiaire } from './new-stagiaire';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';

describe('NewStagiaire', () => {
  let component: NewStagiaire;
  let fixture: ComponentFixture<NewStagiaire>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: TEST_IMPORTS,
      declarations: [NewStagiaire],
      providers: TEST_PROVIDERS,
    }).compileComponents();

    fixture = TestBed.createComponent(NewStagiaire);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
