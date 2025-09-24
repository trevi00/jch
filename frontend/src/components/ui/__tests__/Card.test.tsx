import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '../Card';

describe('Card Components', () => {
  describe('Card', () => {
    it('renders basic card', () => {
      render(<Card data-testid="card">Card content</Card>);
      const card = screen.getByTestId('card');
      expect(card).toBeInTheDocument();
      expect(card).toHaveTextContent('Card content');
    });

    it('applies default variant classes', () => {
      render(<Card data-testid="card">Default card</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('rounded-xl', 'border', 'bg-white', 'shadow-sm');
      expect(card).toHaveClass('border-gray-200');
    });

    it('applies outline variant', () => {
      render(<Card variant="outline" data-testid="card">Outline card</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('border-gray-300');
    });

    it('applies elevated variant', () => {
      render(<Card variant="elevated" data-testid="card">Elevated card</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('shadow-lg');
    });

    it('applies ghost variant', () => {
      render(<Card variant="ghost" data-testid="card">Ghost card</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('border-transparent', 'bg-transparent', 'shadow-none');
    });

    it('applies default padding', () => {
      render(<Card data-testid="card">Default padding</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('p-6');
    });

    it('applies small padding', () => {
      render(<Card padding="sm" data-testid="card">Small padding</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('p-4');
    });

    it('applies large padding', () => {
      render(<Card padding="lg" data-testid="card">Large padding</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('p-8');
    });

    it('applies no padding', () => {
      render(<Card padding="none" data-testid="card">No padding</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('p-0');
    });

    it('applies lift hover effect', () => {
      render(<Card hover="lift" data-testid="card">Hover lift</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('hover:shadow-lg', 'hover:-translate-y-1');
    });

    it('applies glow hover effect', () => {
      render(<Card hover="glow" data-testid="card">Hover glow</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('hover:shadow-lg', 'hover:shadow-blue-500/10');
    });

    it('applies scale hover effect', () => {
      render(<Card hover="scale" data-testid="card">Hover scale</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('hover:scale-[1.02]');
    });

    it('applies custom className', () => {
      render(<Card className="custom-class" data-testid="card">Custom</Card>);
      const card = screen.getByTestId('card');
      expect(card).toHaveClass('custom-class');
    });

    it('forwards HTML div props', () => {
      render(<Card id="test-card" role="article">Card with props</Card>);
      const card = screen.getByRole('article');
      expect(card).toHaveAttribute('id', 'test-card');
    });
  });

  describe('CardHeader', () => {
    it('renders card header', () => {
      render(
        <Card>
          <CardHeader data-testid="header">Header content</CardHeader>
        </Card>
      );
      const header = screen.getByTestId('header');
      expect(header).toBeInTheDocument();
      expect(header).toHaveTextContent('Header content');
      expect(header).toHaveClass('flex', 'flex-col', 'space-y-2');
    });

    it('applies custom className', () => {
      render(
        <CardHeader className="custom-header" data-testid="header">
          Custom header
        </CardHeader>
      );
      const header = screen.getByTestId('header');
      expect(header).toHaveClass('custom-header');
    });
  });

  describe('CardTitle', () => {
    it('renders card title as h3', () => {
      render(
        <Card>
          <CardHeader>
            <CardTitle>Test Title</CardTitle>
          </CardHeader>
        </Card>
      );
      const title = screen.getByRole('heading', { level: 3 });
      expect(title).toBeInTheDocument();
      expect(title).toHaveTextContent('Test Title');
      expect(title).toHaveClass('text-xl', 'font-semibold', 'text-gray-900');
    });

    it('applies custom className', () => {
      render(<CardTitle className="custom-title">Custom Title</CardTitle>);
      const title = screen.getByRole('heading', { level: 3 });
      expect(title).toHaveClass('custom-title');
    });
  });

  describe('CardDescription', () => {
    it('renders card description', () => {
      render(
        <Card>
          <CardHeader>
            <CardDescription>Test description</CardDescription>
          </CardHeader>
        </Card>
      );
      const description = screen.getByText('Test description');
      expect(description).toBeInTheDocument();
      expect(description.tagName).toBe('P');
      expect(description).toHaveClass('text-sm', 'text-gray-600');
    });

    it('applies custom className', () => {
      render(
        <CardDescription className="custom-description">
          Custom description
        </CardDescription>
      );
      const description = screen.getByText('Custom description');
      expect(description).toHaveClass('custom-description');
    });
  });

  describe('CardContent', () => {
    it('renders card content', () => {
      render(
        <Card>
          <CardContent data-testid="content">Content goes here</CardContent>
        </Card>
      );
      const content = screen.getByTestId('content');
      expect(content).toBeInTheDocument();
      expect(content).toHaveTextContent('Content goes here');
      expect(content).toHaveClass('pt-0');
    });

    it('applies custom className', () => {
      render(
        <CardContent className="custom-content" data-testid="content">
          Custom content
        </CardContent>
      );
      const content = screen.getByTestId('content');
      expect(content).toHaveClass('custom-content');
    });
  });

  describe('CardFooter', () => {
    it('renders card footer', () => {
      render(
        <Card>
          <CardFooter data-testid="footer">Footer content</CardFooter>
        </Card>
      );
      const footer = screen.getByTestId('footer');
      expect(footer).toBeInTheDocument();
      expect(footer).toHaveTextContent('Footer content');
      expect(footer).toHaveClass('flex', 'items-center', 'pt-4');
    });

    it('applies custom className', () => {
      render(
        <CardFooter className="custom-footer" data-testid="footer">
          Custom footer
        </CardFooter>
      );
      const footer = screen.getByTestId('footer');
      expect(footer).toHaveClass('custom-footer');
    });
  });

  describe('Complete Card Layout', () => {
    it('renders complete card with all components', () => {
      render(
        <Card data-testid="complete-card">
          <CardHeader>
            <CardTitle>Card Title</CardTitle>
            <CardDescription>Card description text</CardDescription>
          </CardHeader>
          <CardContent>
            <p>This is the main content of the card.</p>
          </CardContent>
          <CardFooter>
            <button>Action Button</button>
          </CardFooter>
        </Card>
      );

      // Check that all parts are present
      expect(screen.getByTestId('complete-card')).toBeInTheDocument();
      expect(screen.getByRole('heading', { name: 'Card Title' })).toBeInTheDocument();
      expect(screen.getByText('Card description text')).toBeInTheDocument();
      expect(screen.getByText('This is the main content of the card.')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'Action Button' })).toBeInTheDocument();
    });

    it('works with minimal card structure', () => {
      render(
        <Card data-testid="minimal-card">
          <CardContent>Simple content</CardContent>
        </Card>
      );

      const card = screen.getByTestId('minimal-card');
      expect(card).toBeInTheDocument();
      expect(screen.getByText('Simple content')).toBeInTheDocument();
    });
  });

  describe('Ref forwarding', () => {
    it('forwards ref to card element', () => {
      const ref = { current: null };
      render(<Card ref={ref as any}>Card with ref</Card>);
      expect(ref.current).toBeInstanceOf(HTMLDivElement);
    });

    it('forwards ref to card header', () => {
      const ref = { current: null };
      render(<CardHeader ref={ref as any}>Header with ref</CardHeader>);
      expect(ref.current).toBeInstanceOf(HTMLDivElement);
    });

    it('forwards ref to card title', () => {
      const ref = { current: null };
      render(<CardTitle ref={ref as any}>Title with ref</CardTitle>);
      expect(ref.current).toBeInstanceOf(HTMLHeadingElement);
    });

    it('forwards ref to card description', () => {
      const ref = { current: null };
      render(<CardDescription ref={ref as any}>Description with ref</CardDescription>);
      expect(ref.current).toBeInstanceOf(HTMLParagraphElement);
    });

    it('forwards ref to card content', () => {
      const ref = { current: null };
      render(<CardContent ref={ref as any}>Content with ref</CardContent>);
      expect(ref.current).toBeInstanceOf(HTMLDivElement);
    });

    it('forwards ref to card footer', () => {
      const ref = { current: null };
      render(<CardFooter ref={ref as any}>Footer with ref</CardFooter>);
      expect(ref.current).toBeInstanceOf(HTMLDivElement);
    });
  });
});