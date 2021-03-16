describe('Base tests', () => {
  it('should login', () => {
    cy.visit(Cypress.env('JAHIA_URL'), {failOnStatusCode: false});
        cy.get('input[name=username]').type('root');
        cy.get('input[name=password]').type(Cypress.env('JAHIA_PASSWORD'));
        cy.get('button[type=submit]').click();
        cy.url().should('include', '/jahia/dashboard')
  })

  it('should not login', () => {
    cy.visit(Cypress.env('JAHIA_URL'), {failOnStatusCode: false});
        cy.get('input[name=username]').type('root');
        cy.get('input[name=password]').type('xxxx');
        cy.get('button[type=submit]').click();
        cy.contains('Invalid username/password')
  })
})