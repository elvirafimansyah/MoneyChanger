using System;
using System.Collections.Generic;
using Microsoft.EntityFrameworkCore;

namespace MoneyChanger.Models;

public partial class MoneyChangerDbContext : DbContext
{
    public MoneyChangerDbContext()
    {
    }

    public MoneyChangerDbContext(DbContextOptions<MoneyChangerDbContext> options)
        : base(options)
    {
    }

    public virtual DbSet<Currency> Currencies { get; set; }

    public virtual DbSet<Order> Orders { get; set; }

    public virtual DbSet<UsdRate> UsdRates { get; set; }

    protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. You can avoid scaffolding the connection string by using the Name= syntax to read it from configuration - see https://go.microsoft.com/fwlink/?linkid=2131148. For more guidance on storing connection strings, see https://go.microsoft.com/fwlink/?LinkId=723263.
        => optionsBuilder.UseSqlServer("Data Source=localhost\\SQLEXPRESS;Initial Catalog=MoneyChangerDB;Integrated Security=True;Encrypt=False;Trust Server Certificate=True");

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Currency>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Currency__3213E83FA54435EC");

            entity.ToTable("Currency");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Abbreviation)
                .HasMaxLength(5)
                .IsUnicode(false)
                .HasColumnName("abbreviation");
            entity.Property(e => e.Country)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("country");
            entity.Property(e => e.Name)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("name");
        });

        modelBuilder.Entity<Order>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__Order__3213E83F8B947796");

            entity.ToTable("Order");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.Code)
                .HasMaxLength(100)
                .IsUnicode(false)
                .HasColumnName("code");
            entity.Property(e => e.ConversionRate)
                .HasColumnType("decimal(38, 10)")
                .HasColumnName("conversion_rate");
            entity.Property(e => e.OrderDate)
                .HasColumnType("datetime")
                .HasColumnName("order_date");
            entity.Property(e => e.OriginCurrencyId).HasColumnName("origin_currency_id");
            entity.Property(e => e.OriginNominal)
                .HasColumnType("decimal(38, 10)")
                .HasColumnName("origin_nominal");
            entity.Property(e => e.TargetCurrencyId).HasColumnName("target_currency_id");
            entity.Property(e => e.TargetNominal)
                .HasColumnType("decimal(38, 10)")
                .HasColumnName("target_nominal");

            entity.HasOne(d => d.OriginCurrency).WithMany(p => p.OrderOriginCurrencies)
                .HasForeignKey(d => d.OriginCurrencyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__Order__origin_cu__3C69FB99");

            entity.HasOne(d => d.TargetCurrency).WithMany(p => p.OrderTargetCurrencies)
                .HasForeignKey(d => d.TargetCurrencyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__Order__target_cu__3D5E1FD2");
        });

        modelBuilder.Entity<UsdRate>(entity =>
        {
            entity.HasKey(e => e.Id).HasName("PK__UsdRate__3213E83F7DC9B986");

            entity.ToTable("UsdRate");

            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CurrencyId).HasColumnName("currency_id");
            entity.Property(e => e.Rate)
                .HasColumnType("decimal(38, 10)")
                .HasColumnName("rate");

            entity.HasOne(d => d.Currency).WithMany(p => p.UsdRates)
                .HasForeignKey(d => d.CurrencyId)
                .OnDelete(DeleteBehavior.ClientSetNull)
                .HasConstraintName("FK__UsdRate__currenc__398D8EEE");
        });

        OnModelCreatingPartial(modelBuilder);
    }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder);
}
